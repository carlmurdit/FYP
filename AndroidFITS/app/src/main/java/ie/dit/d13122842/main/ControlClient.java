package ie.dit.d13122842.main;

import android.os.Handler;
import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.ArrayList;

import ie.dit.d13122842.config.Config;
import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.messages.WorkMessage;
import ie.dit.d13122842.posting.FormPoster;
import ie.dit.d13122842.utils.Timer;
import ie.dit.d13122842.utils.Utils;

public class ControlClient implements Runnable {

    private final boolean AUTOACK_ON = true;
    private final boolean AUTOACK_OFF = false;
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
        int ctli = 0;
        int wrki = 0;

        while (true) {
            try {

                ctli++;

                Utils.tellUI(handler, Enums.UITarget.CTL_HEAD, "No work");
                Utils.tellUI(handler, Enums.UITarget.CTL_STATUS, "Connecting...");
                Utils.tellUI(handler, Enums.UITarget.ERROR, "");

                // create a connection
                Connection connection = factory.newConnection();

                // Create a channel for Control messages
                final Channel channelCTL = connection.createChannel();
                channelCTL.queueDeclare(Config.MQ.QUEUE_NAME_CTL, true, false, false, null);
                channelCTL.basicQos(1); // prefetch count

                // Subscribe to receive messages from the Control Queue
                QueueingConsumer consumer = new QueueingConsumer(channelCTL);
                channelCTL.basicConsume(Config.MQ.QUEUE_NAME_CTL, AUTOACK_ON, consumer);

                // Get a control message

                // while (true) {
                sMsg="Waiting for instructions...";
                Log.d("fyp", sMsg);
                Utils.tellUI(handler, Enums.UITarget.CTL_STATUS, sMsg);

                // Block until next Control message received
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                final String messageCTL = new String(delivery.getBody());
                ControlMessage ctlMsg = new ControlMessage(messageCTL);
                Log.d("fyp", "Control Message parsed:\n" + ctlMsg.toString() + "\n");
                Utils.tellUI(handler, Enums.UITarget.CTL_HEAD, ctli+ "_" + ctlMsg.getDesc());

                // log the messages we are receiving
                Log.d("fyp", "CTL nextDelivery() was iteration "+ctli+" for " +ctlMsg.getDesc());

                // Populate Stars with config data and flat/bias pixels
                ArrayList<Star> stars;
                try {
                    stars = getStars(ctlMsg);
                } catch (Exception e) {
                    throw new Exception("Could not load config data. Cannot process work. "+e.getMessage(),e);
                }

                // create a channel for Work messages
                final Channel channelWRK = connection.createChannel();
                channelWRK.queueDeclare(ctlMsg.getWork_Q_Name(), true, false, false, null);
                channelWRK.basicQos(1); // prefetch count

                // Create the QueueingConsumer and have it consume from the queue
                QueueingConsumer consumerWRK = new QueueingConsumer(channelWRK);
                channelWRK.basicConsume(ctlMsg.getWork_Q_Name(), AUTOACK_OFF, consumerWRK);

                // create a channel for Result messages
                final Channel channelResult = connection.createChannel();
                channelResult.queueDeclare(ctlMsg.getResult_Q_Name(), true, false, false, null);
                // channelResult.basicQos(1); // prefetch count

                Utils.tellUI(handler, Enums.UITarget.CTL_STATUS, "Active");

                while (true) {

                    Utils.tellUI(handler, Enums.UITarget.WRK_HEAD, "No Work");
                    Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, "Waiting for instructions...");

                    // Wait for and process each WORK message
                    QueueingConsumer.Delivery deliveryWRK = consumerWRK.nextDelivery();
                    final String rawMessageWRK = new String(deliveryWRK.getBody());

                    Log.d("fyp", "RECEIVED WORK MESSAGE");
                    // log the messages we are receiving
                    wrki++;
                    Log.e("fyp", "WRK nextDelivery() was iteration " + wrki + " for " +new WorkMessage(rawMessageWRK).getFilename());

                    // Start timing for this work unit (e.g. Fits File)
                    Timer timer = new Timer();
                    timer.start();

                    if (ctlMsg.getActID().compareTo("1")==0) {
                        // Activity is FITS Cleaning
                        Cleaner cleaner = new Cleaner(handler);
                        cleaner.doWork(ctlMsg, stars, rawMessageWRK, channelResult, androidId);
                    } else if (ctlMsg.getActID().compareTo("2")==0) {
                        // Activity is Magnitude Calculation
                        Magnitude magnitude = new Magnitude(handler);
                        magnitude.doWork();
                    } else {
                        Log.d("fyp", "ctlMsg.getCID()="+ctlMsg.getActID());
                    }

                    // tell MQ it can delete the message
                    channelWRK.basicAck(deliveryWRK.getEnvelope().getDeliveryTag(), false);

                    // Update the History and its UI controls
                    History.insert(timer.stop());
                    Utils.tellUI(handler, Enums.UITarget.SUMMARY_1, String.format("%d", History.getUnitCount()));
                    Utils.tellUI(handler, Enums.UITarget.SUMMARY_2, String.format("%d ms.", History.getAverageTime()));

                }


            } catch (InterruptedException e) {
                sMsg = "Processing was interrupted.";
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.RESETALL);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                break;
            } catch (ShutdownSignalException e) {
                sMsg = "The connection was shut down while waiting for messages. " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                break;
            } catch (ConsumerCancelledException e) {
                sMsg = "The consumer was cancelled while waiting for messages. " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                break;
            } catch (IOException e) {
                sMsg = "IOException: " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                break;
            } catch (Exception e) {
                sMsg = "Connection broken, retrying: \n" + e.getClass().getName() + ", " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                try {
                    Thread.sleep(4000); //sleep and then try again
                } catch (InterruptedException ie) {
                    break;
                }
            }

        }
    }


    private ArrayList<Star> getStars(ControlMessage ctlMsg) throws Exception {

        String sMsg; // used for debug and info messages

        // Download the config (star list) file
        sMsg = "Requesting "+ctlMsg.getConfig_Filename()+" from " + ctlMsg.getAPI_Server_URL() + "...";
        Log.d("fyp", sMsg);
        Utils.tellUI(handler, Enums.UITarget.CTL_HEAD, ctlMsg.getDesc());
        Utils.tellUI(handler, Enums.UITarget.CTL_STATUS, "Downloading "+ctlMsg.getConfig_Filename()+"...");
        FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
        poster.add("action", "getfile");  // set POST variables
        poster.add("filename", ctlMsg.getConfig_Filename());
        String configContents = poster.post();

        // parse the contents into stars
        final ArrayList<Star> stars = new ArrayList<>();
        String[] lines = configContents.split("\n"); //todo handle \r\n endings?
        if (lines.length==0)
            throw new Exception("The Config does not contain any stars: "+configContents);
        int lineNum = 1;
        for (String line : lines) {
            if (!line.startsWith("!") && line.trim().length() > 0) {
                stars.add(new Star(lineNum, line));
                lineNum++;
            }
        }

        // display them
        sMsg = "CONFIG Received:\n";
        for (int i=0; i<stars.size(); i++) {
            Star star = stars.get(i);
            sMsg+=String.format("Star %d: x%d, y%d, boxwidth %d\n",
                    i, star.getX(), star.getY(), star.getBoxwidth());
        }
        Log.d("fyp", sMsg);

        // Update each Star with flat and bias boxes from the server
        for (int i=0; i<stars.size(); i++) {
            Star star = stars.get(i);

            // Get the box around this star from the Flat file
            sMsg = String.format("GETTING FLAT, STAR %d...\nX %d, Y %d, Box width %d, %s from %s...",
                    i+1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getFlat_Filename());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.CTL_STATUS, "Downloading "+ctlMsg.getFlat_Filename()+"...");
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");  // add POST variables
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getFlat_Filename());
            poster.add("plane", "1");
            String flatResponse = poster.post();

            // populate star's Flat array from the returned data
            star.setFlatPixels(PixelBox.stringToArray(star.getBoxwidth(), flatResponse));
            Log.d("fyp", "FLAT RECEIVED.");
            longLogv("fyp", PixelBox.arrayToString(star.getFlatPixels(), "-"));

            // repeat for Bias
            sMsg = String.format("GETTING BIAS, STAR %d:\nX %d, Y %d, Box width %d, %s from %s...",
                    i+1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getBias_Filename());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.CTL_STATUS, "Downloading "+ctlMsg.getBias_Filename()+"...");
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
            poster.add("plane", "1");
            String biasResponse = poster.post();

            // populate star's Bias array from the returned data
            star.setBiasPixels(PixelBox.stringToArray(star.getBoxwidth(), biasResponse));
            Log.d("fyp", "BIAS RECEIVED.");
            longLogv("fyp", PixelBox.arrayToString(star.getBiasPixels(), "-"));

        }

        return stars;

    }

    public void longLogv(String tag, String str) {
        if(true) return; // disable
        // avoid max length of logcat messages
        if(str.length() > 4000) {
            Log.v(tag, str.substring(0, 4000));
            longLogv(tag, str.substring(4000));
        } else
            Log.v(tag, str);
    }

}
