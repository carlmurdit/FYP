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
import java.util.ArrayList;

import ie.dit.d13122842.config.Config;
import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.posting.FormPoster;
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
    private ArrayList<Star> stars = null;  // Contents of the Config File

    public ControlClient(Handler handler, ConnectionFactory factory, String androidId) {
        this.handler = handler;
        this.factory = factory;
        this.androidId = androidId;
    }

    @Override
    public void run() {
        String sMsg; // used for debug and info messages

        while (true) {
            try {

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
                    if (actMsg.getActID().compareTo("1") == 0) {
                    // Config only required for FITS Cleaning
                        try {
                            stars = getStars(actMsg);
                        } catch (Exception e) {
                            stars = null;
                            throw new Exception("Could not load config data. Cannot process work. " + e.getMessage(), e);
                        }
                    }

                    // create a channel for Work messages
                    final Channel channelWRK = connection.createChannel();
                    channelWRK.queueDeclare(actMsg.getWork_Q_Name(), true, false, false, null);
                    channelWRK.basicQos(1); // max 1 unacknowledged message at a time

                    // create a channel for Result messages
                    final Channel channelResult = connection.createChannel();
                    channelResult.queueDeclare(actMsg.getResult_Q_Name(), true, false, false, null);

                    Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Active");
                    GetResponse response = channelWRK.basicGet(actMsg.getWork_Q_Name(), AUTOACK_OFF);
                    String rawMessageWRK;
                    if (response == null) {
                        // No message retrieved so re-queue the Activation Message
                        Log.e("fyp", "No WRK Message Found!");
                        channelACT.basicNack(deliveryACT.getEnvelope().getDeliveryTag(), MULTIPLE_OFF, RE_QUEUE_ON);
                        Utils.tellUI(handler, Enums.UITarget.WRK_STATUS_1, "No Work Found for Activity");
                        Thread.sleep(500); // ToDo: Remove if not an issue
                        continue; // go get next CTL message
                    } else {
                        Log.d("fyp", "RECEIVED WORK MESSAGE");
                        rawMessageWRK = new String(response.getBody());
                    }

                    // Start timing for this work unit (i.e. Fits File)
                    Timer timer = new Timer();
                    timer.start();

                    if (actMsg.getActID().compareTo("1") == 0) {
                        // Activity is FITS Cleaning
                        Cleaner cleaner = new Cleaner(handler);
                        cleaner.doWork(actMsg, stars, rawMessageWRK, channelResult, androidId);
                    } else if (actMsg.getActID().compareTo("2") == 0) {
                        // Activity is Magnitude Calculation
                        Magnitude magnitude = new Magnitude(handler);
                        magnitude.doWork();
                    } else {
                        Log.d("fyp", "ctlMsg.getActID()=" + actMsg.getActID());
                    }

                    // tell MQ it can delete the message
                    channelACT.basicAck(deliveryACT.getEnvelope().getDeliveryTag(), false);
                    channelWRK.basicAck(response.getEnvelope().getDeliveryTag(), false);

                    // Update the History and its UI controls
                    History.insert(timer.stop());
                    Utils.tellUI(handler, Enums.UITarget.SUMMARY_1, String.format("%d", History.getUnitCount()));
                    Utils.tellUI(handler, Enums.UITarget.SUMMARY_2, String.format("%d ms.", History.getAverageTime()));

                } // end while


            } catch (InterruptedException e) {
                sMsg = "Processing was interrupted.";
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.RESETALL);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                stars = null; // require re-download
                break;
            } catch (ShutdownSignalException e) {
                sMsg = "The connection was shut down while waiting for messages. " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                stars = null; // require re-download
                break;
            } catch (ConsumerCancelledException e) {
                sMsg = "The consumer was cancelled while waiting for messages. " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                stars = null; // require re-download
                break;
            } catch (IOException e) {
                sMsg = "IOException: " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                stars = null; // require re-download
                break;
            } catch (Exception e) {
                sMsg = "Connection broken, retrying: \n" + e.getClass().getName() + ", " + e.getMessage();
                Log.e("fyp", sMsg, e);
                Utils.tellUI(handler, Enums.UITarget.ERROR, sMsg);
                stars = null; // require re-download
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
        Utils.tellUI(handler, Enums.UITarget.ACT_HEAD, ctlMsg.getDesc());
        Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Downloading "+ctlMsg.getConfig_Filename()+"...");
        FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
        poster.add("action", "getfile");  // set POST variables
        poster.add("filename", ctlMsg.getConfig_Filename());
        String configContents = poster.post();

        // parse the contents into stars
        ArrayList<Star> stars = new ArrayList<>();
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

        // Compare previously downloaded Config (if found) to this download
        if (this.stars != null && this.stars.size() == stars.size()) {
            boolean differs = false;
            for (int i=0; i<stars.size(); i++) {
                Star star = stars.get(i);
                Star oldStar = this.stars.get(i);
                if (star.getBox().compareTo(oldStar.getBox()) != 0) {
                    // difference found, so re-download flat and bias
                    differs = true;
                    break;
                }
            }
            // all same so no need to re-download
            if (!differs) return this.stars;
        }

        // Update each Star with flat and bias boxes from the server
        for (int i=0; i<stars.size(); i++) {
            Star star = stars.get(i);

            // Get the box around this star from the Flat file
            sMsg = String.format("GETTING FLAT, STAR %d...\nX %d, Y %d, Box width %d, %s from %s...",
                    i+1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getFlat_Filename());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Downloading "+ctlMsg.getFlat_Filename()+"...");
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");  // add POST variables
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getFlat_Filename());
            poster.add("plane", "1");
            String flatResponse = poster.post();

            // populate star's Flat array from the returned data
            star.setFlatPixels(PixelBox.stringToArray(star.getBoxwidth(), flatResponse));
            Log.d("fyp", "FLAT RECEIVED.");
            Utils.longLogV("fyp", PixelBox.arrayToString(star.getFlatPixels(), "-"), false);

            // repeat for Bias
            sMsg = String.format("GETTING BIAS, STAR %d:\nX %d, Y %d, Box width %d, %s from %s...",
                    i+1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getBias_Filename());
            Log.d("fyp", sMsg);
            Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Downloading "+ctlMsg.getBias_Filename()+"...");
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
            poster.add("plane", "1");
            String biasResponse = poster.post();

            // populate star's Bias array from the returned data
            star.setBiasPixels(PixelBox.stringToArray(star.getBoxwidth(), biasResponse));
            Log.d("fyp", "BIAS RECEIVED.");
            Utils.longLogV("fyp", PixelBox.arrayToString(star.getBiasPixels(), "-"), false);

        }

        return stars;

    }

}
