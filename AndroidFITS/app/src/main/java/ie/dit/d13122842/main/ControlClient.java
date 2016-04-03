package ie.dit.d13122842.main;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

import ie.dit.d13122842.config.Config;
import ie.dit.d13122842.exception.ActivationException;
import ie.dit.d13122842.exception.ResultPublicationException;
import ie.dit.d13122842.exception.WorkException;
import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.utils.Timer;
import ie.dit.d13122842.utils.Utils;

public class ControlClient implements Runnable {

    private final boolean AUTOACK_ON = true;
    private final boolean AUTOACK_OFF = false;
    boolean MULTIPLE_OFF = false;
    boolean RE_QUEUE_ON = true;
    private final Handler handler;
    private final ConnectionFactory factory;
    private final String androidId;

    public ControlClient(Handler handler, ConnectionFactory factory, String androidId) {
        this.handler = handler;
        this.factory = factory;
        this.androidId = androidId;
    }

    @Override
    public void run() {
        String sMsg; // used for debug and info messages
        boolean starsDownloaded = false;
        boolean biasAndFlatDownloaded = false;
        Stars stars = new Stars();

        while (true) {
            try {

                // initialise the work status in the UI
                Utils.tellUI(handler, Enums.UITarget.ACT_HEAD, "No work");
                Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Connecting...");
                Utils.tellUI(handler, Enums.UITarget.ERROR, "");

                // create a connection
                Connection connection = factory.newConnection();

                // Create a channel for Control messages
                final Channel channelACT = connection.createChannel();
                channelACT.queueDeclare(Config.MQ.QUEUE_NAME_ACT, true, false, false, null);
                channelACT.basicQos(1); // max 1 unacknowledged message at a time

                // Subscribe to receive messages from the Activation Queue
                QueueingConsumer consumerACT = new QueueingConsumer(channelACT);
                channelACT.basicConsume(Config.MQ.QUEUE_NAME_ACT, AUTOACK_OFF, consumerACT);

                while (true) {
                    // Loop after every Activation message

                    sMsg = "Waiting for instructions...";
                    Log.d("fyp", sMsg);
                    Utils.tellUI(handler, Enums.UITarget.ACT_HEAD, "Idle");
                    Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, sMsg);
                    Utils.tellUI(handler, Enums.UITarget.WRK_HEAD, "");
                    Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, "");

                    // Block until next Activation message received
                    QueueingConsumer.Delivery deliveryACT = consumerACT.nextDelivery();

                    // Parse and display the Activity
                    final String messageACT = new String(deliveryACT.getBody());
                    ControlMessage actMsg = new ControlMessage(messageACT);
                    Log.d("fyp", "Activation Message parsed:\n" + actMsg.toString() + "\n");
                    Utils.tellUI(handler, Enums.UITarget.ACT_HEAD, actMsg.getDesc());
                    Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Initialising Activity...");

                    // Download Config then get flat/bias if we don't have them
                    try {
                        if (!starsDownloaded) {
                            biasAndFlatDownloaded = false; // bias & flat depend on stars
                            stars.populateFromConfig(handler, actMsg);
                            starsDownloaded = true;
                        }
                    } catch (Exception e) {
                        starsDownloaded = false;
                        throw new ActivationException("Could not load config data. Cannot process work. " + e.getMessage(), e);
                    }

                    // Download flat/bias if we need them and don't have them
                    try {
                        if (actMsg.getActID().equalsIgnoreCase(Enums.Activities.CLEANING)) {
                            if (!biasAndFlatDownloaded) {
                                stars.populateWithBiasAndFlat(handler, actMsg);
                            }
                            biasAndFlatDownloaded = true;
                        }
                    } catch (Exception e) {
                        starsDownloaded = false;
                        throw new ActivationException("Could not load bias / flat. Cannot process work. " + e.getMessage(), e);
                    }

                    // create a channel for Work messages
                    final Channel channelWRK = connection.createChannel();
                    channelWRK.queueDeclare(actMsg.getWork_Q_Name(), true, false, false, null);
                    channelWRK.basicQos(1); // max 1 unacknowledged message at a time

                    // create a channel for Result messages
                    final Channel channelResult = connection.createChannel();
                    channelResult.queueDeclare(actMsg.getResult_Q_Name(), true, false, false, null);

                    Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Active");
                    GetResponse responseWRK = channelWRK.basicGet(actMsg.getWork_Q_Name(), AUTOACK_OFF);
                    String rawMessageWRK;
                    if (responseWRK == null) {
                        // No message retrieved so re-queue the Activation Message
                        Log.e("fyp", "No WRK Message Found!");
                        channelACT.basicNack(deliveryACT.getEnvelope().getDeliveryTag(), MULTIPLE_OFF, RE_QUEUE_ON);
                        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, "No Work Found for Activity");
                        Thread.sleep(1000); // pause before message is reset
                        continue; // go get next CTL message
                    } else {
                        Log.d("fyp", "RECEIVED WORK MESSAGE");
                        rawMessageWRK = new String(responseWRK.getBody());
                    }

                    // Start timing for this work unit (i.e. Fits File)
                    Timer timer = new Timer();
                    timer.start();

                    try {
                        if (actMsg.getActID().compareTo(Enums.Activities.CLEANING) == 0) {

                            // Activity is FITS Cleaning
                            Cleaner cleaner = new Cleaner(handler);
                            cleaner.doWork(actMsg, stars, rawMessageWRK, channelResult, androidId);
                        } else if (actMsg.getActID().compareTo(Enums.Activities.MAGNITUDE) == 0) {

                            // Activity is Magnitude Calculation
                            Magnitude magnitude = new Magnitude(handler);
                            magnitude.doWork(actMsg, stars, rawMessageWRK, channelResult, androidId);
                        } else {

                            // Unexpected activity!
                            throw new WorkException("Unexpected Activity, ID: " + actMsg.getActID());
                        }
                    } catch (WorkException e) {
                        sMsg = e.getMessage();
                        Log.e("fyp", sMsg, e);
                        Utils.tellUI(handler, Enums.UITarget.RESETALL);
                        Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                        stars = null; // require re-download
                        // reject the Activity / Work Unit pair
                        channelACT.basicNack(deliveryACT.getEnvelope().getDeliveryTag(), MULTIPLE_OFF, RE_QUEUE_ON);
                        channelWRK.basicNack(responseWRK.getEnvelope().getDeliveryTag(), MULTIPLE_OFF, RE_QUEUE_ON);
                        starsDownloaded = false;
                        Thread.currentThread().sleep(1000);  // show error
                        continue;
                    }

                    // tell MQ it can delete the message
                    channelACT.basicAck(deliveryACT.getEnvelope().getDeliveryTag(), false);
                    channelWRK.basicAck(responseWRK.getEnvelope().getDeliveryTag(), false);

                    // Update the History and its UI controls
                    History.insert(timer.stop());
                    Utils.tellUI(handler, Enums.UITarget.SUMMARY_1, String.format("%d", History.getUnitCount()));
                    Utils.tellUI(handler, Enums.UITarget.SUMMARY_2, String.format("%d ms.", History.getAverageTime()));

                } // end while

            } catch (ActivationException e) {
                sMsg = "Activity Initialisation failed. "+e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.RESETALL);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                starsDownloaded = false; // require re-download
                break;
            } catch (ResultPublicationException e) {
                // publishing a Result message failed - RabbitMQ reset required
                sMsg = "Error publishing result message: "+e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.RESETALL);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                break;
            } catch (InterruptedException e) {
                sMsg = "Processing was interrupted.";
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.RESETALL);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                starsDownloaded = false; // require re-download
                break;
            } catch (ShutdownSignalException e) {
                sMsg = "The connection was shut down while waiting for messages. " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                starsDownloaded = false; // require re-download
                break;
            } catch (ConsumerCancelledException e) {
                sMsg = "The consumer was cancelled while waiting for messages. " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                starsDownloaded = false; // require re-download
                break;
            } catch (IOException e) {
                sMsg = "IOException: " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                starsDownloaded = false; // require re-download
                break;
            } catch (Exception e) {
                sMsg = "Connection broken, retrying: \n" + e.getClass().getName() + ", " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                starsDownloaded = false; // require re-download
                try {
                    Thread.sleep(4000); //sleep and then try again
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }



}
