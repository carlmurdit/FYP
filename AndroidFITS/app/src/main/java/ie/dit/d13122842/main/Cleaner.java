package ie.dit.d13122842.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class Cleaner implements Runnable {

    private final boolean AUTOACK_ON = true;
    private final boolean AUTOACK_OFF = false;
    private final Handler handler;
    private final ConnectionFactory factory;

    public Cleaner(Handler handler, ConnectionFactory factory) {
        this.handler = handler;
        this.factory = factory;
    }

    private void tellUI(String msg) {
        // send message to the UI thread
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    public void run() {
        String sMsg; // used for debug and info messages

        while (true) {
            try {
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
                sMsg="Waiting for control message...";
                Log.d("fyp", sMsg);
                tellUI(sMsg + "\n");

                // Block until next Control message received
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                final String messageCTL = new String(delivery.getBody());
                Log.d("fyp", "CONTROL MESSAGE RECEIVED.");
                ControlMessage ctlMsg = new ControlMessage(messageCTL, delivery.getEnvelope().getDeliveryTag());
                Log.d("fyp", "Control Message parsed:\n" + ctlMsg.toString() + "\n");
                tellUI("CONTROL MESSAGE RECEIVED:\n" + ctlMsg.toString() + "\n");

                // channelCTL.basicAck(ctlMsg.getDeliveryTag(), true);
                // tell rabbitMQ to requeue the message
                // channelCTL.basicNack(ctlMsg.getDeliveryTag(), false, true);
                // channelCTL.basicReject(ctlMsg.getDeliveryTag(), true); // doesn't work! Stays un-acked.
                // Log.d("fyp", "basicReject() done "+delivery.getEnvelope().getDeliveryTag()+" "+ctlMsg.getDeliveryTag());
                // todo: Cancel Subscription - we don't want more Control Messages
                // channelCTL.basicCancel(consumer.getConsumerTag());

                // Populate Stars with config data and flat/bias pixels
                ArrayList<Star> stars;
                try {
                    stars = getStars(ctlMsg);
                } catch (Exception e) {
                    throw new Exception("Could not load config data. Cannot process work. "+e.getMessage(),e);
                }



                // create a channel for Work messages
                final Channel channelWRK = connection.createChannel();
                channelWRK.queueDeclare(Config.MQ.QUEUE_NAME_WRK, true, false, false, null);
                channelWRK.basicQos(1); // prefetch count

                // while (true) {
                // get the next WORK message
//                            boolean autoAck = false;
//                            channelWRK.basicConsume(QUEUE_NAME_WRK, autoAck,
//                                    new DefaultConsumer(channelWRK) {
//                                        @Override
//                                        public void handleDelivery(String consumerTag,
//                                                                   Envelope envelope,
//                                                                   AMQP.BasicProperties properties,
//                                                                   byte[] body)
//                                                throws IOException
//                                        {
//                                            String routingKey = envelope.getRoutingKey();
//                                            String contentType = properties.getContentType();
//                                            long deliveryTag = envelope.getDeliveryTag();
//                                            // (process the message components here ...)
//                                            String messageWRK = new String(body, "UTF-8");
//                                            String sMsg = "Got a work message:\n"+messageWRK;
//                                            tellUI(sMsg);
//                                            Log.d("fyp", sMsg);
//
//                                            // WorkMessage wrkMsg = parser.parseWorkMessage(messageWRK, envelope.getDeliveryTag());
//                                            channelWRK.basicAck(deliveryTag, false);
//                                            Log.d("fyp", "After Ack!");
//                                            tellUI("After Ack!");
//
//                                        }
//                                        @Override
//                                        public void handleShutdownSignal(java.lang.String consumerTag,
//                                                                         ShutdownSignalException sig){
//                                            String sMsg = "handleShutdownSignal! "+ sig.getMessage();
//                                            Log.d("fyp", sMsg);
//                                            tellUI(sMsg);
//
//                                        }
//                                    });

                // Create the QueueingConsumer and have it consume from the queue
                QueueingConsumer consumerWRK = new QueueingConsumer(channelWRK);
                channelWRK.basicConsume(Config.MQ.QUEUE_NAME_WRK, AUTOACK_OFF, consumerWRK);

                // create a channel for Result messages
                final Channel channelRLT = connection.createChannel();
                channelRLT.queueDeclare(Config.MQ.QUEUE_NAME_RLT, true, false, false, null);
                channelRLT.basicQos(1); // prefetch count

                while (true) {

                    // Wait for and process each WORK message
                    QueueingConsumer.Delivery deliveryWRK = consumerWRK.nextDelivery();
                    final String messageWRK = new String(deliveryWRK.getBody());
                    Log.d("fyp", "RECEIVED WORK MESSAGE:");
                    WorkMessage wrkMsg = new WorkMessage(messageWRK, deliveryWRK.getEnvelope().getDeliveryTag());
                    Log.d("fyp", "PARSED WORK MESSAGE:\n" + wrkMsg.toString());
                    tellUI("RECEIVED WORK:\n" + wrkMsg.toString() + "\n");

                    // for each star, clean the FITS box in each plane
                    for (Star star : stars) {
                        for (int iPlane=1; iPlane<=wrkMsg.getPlanes(); iPlane++) {

                            // Attempt to process this work unit
                            String resultMessage ="SENDING RESULT...";
                            try {
                                double[][][] resultPixels = cleanBox(ctlMsg, wrkMsg, star, iPlane);
                                // todo Send processed pixels to API Server 2

                                // Send a message to the result queue
                                resultMessage = String.format(
                                        "Cleaned %s, Star %d, Plane %d, X %d, Y %d, Box width %d, %s.",
                                        wrkMsg.getFilename(), star.getStarNum(), iPlane, star.getX(), star.getY(),
                                        star.getBoxwidth(), star.getBox());
                                channelRLT.basicPublish("", Config.MQ.QUEUE_NAME_RLT, null, resultMessage.getBytes());
                                Log.d("fyp", "RESULT SENT.");
                                tellUI("RESULT SENT.\n");
                            } catch (Exception e) {
                                resultMessage = String.format(
                                        "FAIL:\n" +
                                                "%s, Star %d, Plane %d, X %d, Y %d, Box width %d, %s\nERR: %s.\n",
                                        wrkMsg.getFilename(), star.getStarNum(), iPlane, star.getX(), star.getY(),
                                        star.getBoxwidth(), star.getBox(), e.getMessage());
                                channelRLT.basicPublish("", Config.MQ.QUEUE_NAME_RLT, null, resultMessage.getBytes());
                                Log.d("fyp", "FAIL SENT.\nReason" + e.getMessage());
                                tellUI("FAIL SENT.\nReason:" +e.getMessage()+"\n");
                            }
                        }
                    }



                    channelWRK.basicAck(wrkMsg.getDeliveryTag(), true);

                    // channelWRK.basicAck(deliveryWRK.getEnvelope().getDeliveryTag(), false);
                }

//                            WorkMessage wrkMsg;
//
//                            GetResponse response = channelWRK.basicGet(QUEUE_NAME_WRK, AUTOACK_OFF);
//                            if (response == null) {
//                                tellUI("-> No Work Message found.");
//                                Log.d("fyp", "\t-> Work Message found.\n");
//                            } else {
//                                String messageWRK = new String(response.getBody(), "UTF-8");
//                                Log.d("fyp", "-> Work message received.");
//                                wrkMsg = parser.parseWorkMessage(messageWRK, response.getEnvelope().getDeliveryTag());
//                                Log.d("fyp", "-> Work message parsed:\n" + wrkMsg.toString());
//                                tellUI("-> Work message received:\n" + wrkMsg.toString() + "\n");
//                            }
//
//                            WorkingData workData = getPixels(ctlMsg, wrkMsg);

                // }


                // do the rest...
//                            doWork(ctlMsg);

                //ack work message
                // ack(wrkMsg, channelWRK);

//                            sMsg = "All done!";
//                            Log.d("fyp", sMsg);
//                            tellUI(sMsg);

                // }
            } catch (InterruptedException e) {
                sMsg = "-> subscribeThread was interrupted.";
                Log.e("fyp", sMsg);
                tellUI(sMsg);
                break;
            } catch (ShutdownSignalException e) {
                Log.e("fyp", "-> The connection was shut down while waiting for messages. " + e.getMessage());
            } catch (ConsumerCancelledException e) {
                Log.e("fyp", "-> The consumer was cancelled while waiting for messages. " + e.getMessage());
            } catch (IOException e) {
                Log.e("fyp", "-> IOException: " + e.getMessage(), e);
            } catch (Exception e1) {
                Log.d("fyp", "->  Connection broken: " + e1.getClass().getName() + ", " + e1.getMessage());
                Log.d("fyp", e1.toString());
                try {
                    Thread.sleep(4000); //sleep and then try again
                } catch (InterruptedException e) {
                    break;
                }
            }
            // subscribeThread = null;
        }
    }



    private ArrayList<Star> getStars(ControlMessage ctlMsg) throws Exception {

        String sMsg; // used for debug and info messages

        // Download the config (star list) file
        sMsg = "Requesting "+ctlMsg.getConfig_Filename()+" from " + ctlMsg.getAPI_Server_URL() + "...";
        Log.d("fyp", sMsg);
        tellUI(sMsg);
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
        Log.d("fyp",sMsg);
        tellUI(sMsg);

        // Update each Star with flat and bias boxes from the server
        for (int i=0; i<stars.size(); i++) {
            Star star = stars.get(i);

            // Get the box around this star from the Flat file
            sMsg = String.format("GETTING FLAT, STAR %d...\nX %d, Y %d, Box width %d, %s from %s...",
                    i+1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getFlat_Filename());
            Log.d("fyp",sMsg);
            tellUI(sMsg);
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");  // add POST variables
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getFlat_Filename());
            poster.add("plane", "1");
            String flatResponse = poster.post();

            // populate star's Flat array from the returned data
            star.setFlatPixels(PixelBox.stringToArray(star.getBoxwidth(), flatResponse));
            tellUI("FLAT RECEIVED.\n");
            Log.d("fyp", "FLAT RECEIVED.");
            longLogv("fyp", PixelBox.arrayToString(star.getFlatPixels(), "-"));

            // repeat for Bias
            sMsg = String.format("GETTING BIAS, STAR %d:\nX %d, Y %d, Box width %d, %s from %s...",
                    i+1, star.getX(), star.getY(), star.getBoxwidth(), star.getBox(), ctlMsg.getBias_Filename());
            Log.d("fyp", sMsg);
            tellUI(sMsg);
            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
            poster.add("action", "getbox");
            poster.add("box", star.getBox());
            poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
            poster.add("plane", "1");
            String biasResponse = poster.post();

            // populate star's Bias array from the returned data
            star.setBiasPixels(PixelBox.stringToArray(star.getBoxwidth(), biasResponse));
            tellUI("BIAS RECEIVED.\n");
            Log.d("fyp", "BIAS RECEIVED.");
            longLogv("fyp", PixelBox.arrayToString(star.getBiasPixels(), "-"));

        }

        return stars;

    }

    // For a work message, download and clean a FITS box, using stored Star data
    private double[][][] cleanBox(ControlMessage ctlMsg, WorkMessage wrkMsg, Star star, int plane) throws Exception {
        String sMsg; // used for debug and info messages

        sMsg = String.format("GETTING %s, STAR %d, PLANE %d...\nX %d, Y %d, Box width %d, %s",
                wrkMsg.getFilename(), star.getStarNum(), plane, star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
        Log.d("fyp", sMsg);
        tellUI(sMsg);
        FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
        poster.add("action", "getbox"); // add POST variables
        poster.add("box", star.getBox());
        poster.add("filename", wrkMsg.getFilename());
        poster.add("plane", Integer.toString(plane));
        String fitsResponse = poster.post();

        // populate an array from the returned Fits data
        double[][][] fitsPixels = PixelBox.stringToArray(star.getBoxwidth(), fitsResponse);
        tellUI("FITS RECEIVED.\n");
        Log.d("fyp", "FITS RECEIVED.");
        longLogv("fyp", PixelBox.arrayToString(fitsPixels, Integer.toString(plane)));

        // Perform Calculation on the data
        sMsg = String.format("CLEANING %s, STAR %d, PLANE %d...\nX %d, Y %d, Box width %d, %s",
                wrkMsg.getFilename(), star.getStarNum(), plane, star.getX(), star.getY(), star.getBoxwidth(), star.getBox());
        Log.d("fyp", sMsg);
        tellUI(sMsg);
        double[][][] resultPixels = crunchPixels(fitsPixels, star.getBiasPixels(), star.getFlatPixels(), star.getBoxwidth());
        tellUI("FITS CLEANED.\n");
        Log.d("fyp", "FITS CLEANED.");
        longLogv("fyp", PixelBox.arrayToString(resultPixels, Integer.toString(plane)));

        return resultPixels;

    }

    private double[][][] crunchPixels(double[][][] fitsPixels, double[][][] biasPixels, double[][][] flatPixels, int boxWidth) throws Exception {
        // Process pixels to create new file
        // New pixel = (RAW - Bias) / Flat = new pixel
        int p=0, x=0, y =0;
        double[][][] resultPixels = new double[1][boxWidth][boxWidth];

        try {

            for (p = 0; p < fitsPixels.length; p++) {
                for (x = 0; x < boxWidth; x++) {
                    for (y = 0; y < boxWidth; y++) {
                        resultPixels[p][x][y] = (fitsPixels[p][x][y] - biasPixels[0][x][y]) / flatPixels[0][x][y];
                        // Log.d("fyp", String.format("Crunching: p%d, x%d, y%d, %.10f", p,x,y,resultPixels[p][x][y]));
                    }
                }
            }
            return resultPixels;

        } catch (Exception e) {
            throw new Exception(String.format("Error cleaning pixels at pixel p%d x%d y%d: %s",
                    p, x, y, e.getMessage()));
        }

    }

    private void doWork(ControlMessage ctlMsg) {
        Log.d("fyp", "-> doWork()...");

        // Store result for the message (50 blocks) on AWS
        // Write to result queue with uploaded URL
        // ackWork()
    }

    private void reject(ControlMessage ctlMsg, Channel channelCTL) throws IOException {
        try {
            channelCTL.basicReject(ctlMsg.getDeliveryTag(), true); // Tell RabbitMQ to requeue
        } catch (IOException ioe) {
            throw new IOException("Error rejecting control message: "+ioe.getMessage(), ioe);
        }
    }

    private void ack(WorkMessage wrkMsg, Channel channelWRK) {
        // tell RabbitMQ that the message has been processed
        try {
            channelWRK.basicAck(wrkMsg.getDeliveryTag(), false); // send ack to RabbitMQ
        } catch (IOException e) {
            System.out.println("Error acking: "+e.getMessage());
        }
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
