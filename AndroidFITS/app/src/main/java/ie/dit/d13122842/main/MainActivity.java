package ie.dit.d13122842.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ie.dit.d13122842.messages.ControlMessage;
import ie.dit.d13122842.messages.WorkMessage;
import ie.dit.d13122842.utils.Parser;

public class MainActivity extends AppCompatActivity {

    String queueURI = "amqp://test:test@192.168.3.21:5672";
    //String queueURI = "amqp://test:test@147.252.141.32:5672";
    private final String QUEUE_NAME_CTL = "control_queue";
    private final String QUEUE_NAME_WRK = "work_queue";
    private final boolean AUTOACK_OFF = false;
    private final int WORK_PER_CTRL = 2; // work messages per control message
    Parser parser = new Parser();
    ConnectionFactory factory;

    Button btnReset;
    TextView tvMain;
    ScrollView scrollView;

    Thread subscribeThread = null;
    //  Thread publishThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setTypeface(Typeface.MONOSPACE);

        try {
            factory = new ConnectionFactory();
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(queueURI);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            Log.e("fyp", "-> Error in setupConnectionFactory(): "+e.getMessage());
            e.printStackTrace();
        }

        // Handler to print messages generated within subscribeThread on the UI thread
        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                Date now = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
                tvMain.append(ft.format(now) + ' ' + message + '\n');
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        };

        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btnReset.setText("Stop");
                Log.d("fyp", "Reset tapped.");
                tvMain.setText("");
                subscribe(incomingMessageHandler);
                //if(running) subscribeThread.interrupt();

            }
        });


    }

    void subscribe(final Handler handler) {

        if (subscribeThread != null)
            subscribeThread.interrupt();

        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String sMsg = ""; // used for debug and info messages

                while (true) {
                    try {
                        // create a connection
                        Connection connection = factory.newConnection();

                        // create a channel for Work messages
                        final Channel channelWRK = connection.createChannel();
                        channelWRK.queueDeclare(QUEUE_NAME_WRK, true, false, false, null);
                        channelWRK.basicQos(0); // prefetch count unlimited (because pulled)

                        // Create a channel for Control messages
                        final Channel channelCTL = connection.createChannel();
                        channelCTL.queueDeclare(QUEUE_NAME_CTL, true, false, false, null);
                        channelCTL.basicQos(1); // prefetch count

                        // Subscribe to receive messages from the Control Queue
                        QueueingConsumer consumer = new QueueingConsumer(channelCTL);
                        channelCTL.basicConsume(QUEUE_NAME_CTL, AUTOACK_OFF, consumer);

                        while (true) {
                            sMsg="Waiting for control message...";
                            Log.d("fyp",sMsg);
                            tellUI(sMsg + "\n");

                            // Block until next Control message received
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                            String messageCTL = new String(delivery.getBody());
                            Log.d("fyp", "Control Message received.");
                            ControlMessage ctlMsg = parser.parseControlMessage(messageCTL, delivery.getEnvelope().getDeliveryTag());
                            Log.d("fyp", "Control Message parsed:\n" + ctlMsg.toString());
                            tellUI("-> Control Message received:\n" + ctlMsg.toString() + "\n");

                            channelCTL.basicAck(ctlMsg.getDeliveryTag(), true);
                            // tell rabbitMQ to requeue the message
                            // channelCTL.basicReject(ctlMsg.getDeliveryTag(), true);
                            // todo: Cancel Subscription - we don't want more Control Messages
                            // channelCTL.basicCancel(consumer.getConsumerTag());

                            ArrayList<WorkMessage> wrkMsgs = new ArrayList<WorkMessage>();

                            // get the WORK messages
                            for (int i=1; i<=WORK_PER_CTRL; i++) {
                                GetResponse response = channelWRK.basicGet(QUEUE_NAME_WRK, AUTOACK_OFF);
                                if (response == null) {
                                    tellUI("-> No Work Message found.");
                                    Log.d("fyp", "\t-> Work Message found.\n");
                                } else {
                                    String messageWRK = new String(response.getBody(), "UTF-8");
                                    Log.d("fyp", "-> Work message received.");
                                    WorkMessage wrkMsg = parser.parseWorkMessage(messageWRK, response.getEnvelope().getDeliveryTag());
                                    wrkMsgs.add(wrkMsg);
                                    Log.d("fyp", "-> Work message parsed:\n" + wrkMsg.toString());
                                    tellUI("-> Work message received:\n" + wrkMsg.toString()+"\n");
                                }
                            }

                            WorkingData workData = getPixels(ctlMsg, wrkMsgs);

                            // do the rest...
                            doWork(ctlMsg);

                            //ack all work messages
                            ack(wrkMsgs, channelWRK);

                            sMsg = "All done!";
                            Log.d("fyp", sMsg);
                            tellUI(sMsg);

                        }
                    } catch (InterruptedException e) {
                        sMsg = "-> subscribeThread was interrupted.";
                        Log.e("fyp", sMsg);
                        tellUI(sMsg);
                        break;
                    } catch (ShutdownSignalException e) {
                        Log.e("fyp", "-> The connection was shut down while waiting for messages. "+e.getMessage());
                    } catch (ConsumerCancelledException e) {
                        Log.e("fyp", "-> The consumer was cancelled while waiting for messages. "+e.getMessage());
                    } catch (Exception e1) {
                        Log.d("fyp", "->  Connection broken: " + e1.getClass().getName() + ", " + e1.getMessage());
                        try {
                            Thread.sleep(4000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    subscribeThread = null;
                }
            }

            private void tellUI(String msg) {
                // send message to the UI thread
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("msg", msg);
                message.setData(bundle);
                handler.sendMessage(message);
            }

            private WorkingData getPixels(ControlMessage ctlMsg,
                                   ArrayList<WorkMessage> wrkMsgs) throws Exception {

                String sMsg = ""; // used for debug and info messages

                // Download the config (star list) file
                sMsg = "Requesting "+ctlMsg.getConfig_Filename()+" from " + ctlMsg.getAPI_Server_URL() + "...";
                Log.d("fyp",sMsg);
                tellUI(sMsg);
                FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                poster.add("action", "getfile");  // set POST variables
                poster.add("filename", ctlMsg.getConfig_Filename());
                String configContents = poster.post();

                // parse the contents into stars
                final ArrayList<Star> stars = new ArrayList<Star>();
                parser.parseConfig(configContents, stars);

                // display them
                sMsg = "Config Received:\n";
                for (int i=0; i<stars.size(); i++) {
                    Star star = stars.get(i);
                    sMsg+=String.format("Star %d: x%d, y%d, boxwidth %d\n",
                            i, star.x, star.y, star.boxwidth);
                }
                Log.d("fyp",sMsg);
                tellUI(sMsg);

                if (stars.size() == 0) {
                    throw new Exception("No stars found in Config: "+configContents);
                }

                // Initialise some empty arrays for pixel data
                WorkingData workData = new WorkingData(stars.get(0).boxwidth);

                // For each star in the config, download boxes from flat and bias
//              for (int i=0; i<stars.size(); i++) {
                for (int i=0; i<1; i++) {
                    Star star = stars.get(i);

                    int x1 = star.x - star.boxwidth/2;
                    int y1 = star.y - star.boxwidth/2;
                    int x2 = star.x + star.boxwidth/2 -1;
                    int y2 = star.y + star.boxwidth/2 -1;
                    // see 5.2 FITS File Access Routines, p 39 of 186 in cfitsio user ref guide
                    String box = String.format("[%d:%d,%d:%d]", x1, x2, y1, y2);
                    // todo - remove test data
//                    workData = new WorkingData(10);
//                    tellUI("Orig box: "+box);
//                    box = "[101:110,201:210]";
//                    star.boxwidth = 10;
                    // end todo

                    // Get the box around this star from the Flat file
                    sMsg = String.format("STAR %d: Requesting X %d, Y %d, Box width %d, %s from %s...",
                            i, star.x, star.y, star.boxwidth, box, ctlMsg.getFlat_Filename());
                    Log.d("fyp",sMsg);
                    tellUI(sMsg);
                    poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                    poster.add("action", "getbox");  // add POST variables
                    poster.add("box", box);
                    poster.add("filename", ctlMsg.getFlat_Filename());
                    poster.add("plane", "1");
                    String flatResponse = poster.post();

                    // populate an array from the returned Flat data
                    parser.parsePixels(flatResponse, workData.flatPixels);
                    tellUI("Flat Received.\n");
                    Log.d("fyp", "Flat Received.\n");
                    Log.v("fyp", WorkingData.toString(workData.flatPixels, "-"));

                    // repeat for Bias
                    // todo - remove test data
                    // box = "[151:160,251:260]";
                    // end todo
                    sMsg = String.format("STAR %d: Requesting X %d, Y %d, Box width %d, %s from %s...",
                            i, star.x, star.y, star.boxwidth, box, ctlMsg.getBias_Filename());
                    Log.d("fyp", sMsg);
                    tellUI(sMsg);
                    poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                    poster.add("action", "getbox");
                    poster.add("box", box);
                    poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
                    poster.add("plane", "1");
                    String biasResponse = poster.post();

                    // populate an array from the returned Bias data
                    parser.parsePixels(biasResponse, workData.biasPixels);
                    tellUI("Bias Received.\n");
                    Log.d("fyp", "Bias Received.\n");
                    Log.v("fyp", WorkingData.toString(workData.biasPixels, "-"));


                    for (WorkMessage wm : wrkMsgs) {
                        for (int plane = 1; plane <= 2; plane++) {
//                        for (int plane = 1; plane <= wm.getPlanes(); plane++) {

                            // todo - remove test data
//                            box = "[101:110,201:210]";
                            // end todo
                            // get the box from the FITS file in the Work Message
                            sMsg = String.format("STAR %d: Requesting plane %d, X %d, Y %d, Box width %d, %s from %s...",
                                    i, plane, star.x, star.y, star.boxwidth, box, wm.getFilename());
                            Log.d("fyp", sMsg);
                            tellUI(sMsg);
                            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                            poster.add("action", "getbox");
                            poster.add("box", box);
                            poster.add("filename", wm.getFilename()); // add POST variables
                            poster.add("plane", Integer.toString(plane));
                            String fitsResponse = poster.post();

                            // populate an array from the returned Fits data
                            parser.parsePixels(fitsResponse, workData.fitsPixels);
                            tellUI("FITS Received.\n");
                            Log.d("fyp", "FITS Received.\n");
                            Log.v("fyp", WorkingData.toString(workData.fitsPixels, Integer.toString(plane)));


                            // Perform Calculation on the data
                            sMsg = String.format("Cleaning pixels:\n" +
                                            "STAR %d, plane %d, X %d, Y %d, Box width %d, %s from %s...",
                                    i, plane, star.x, star.y, star.boxwidth, box, wm.getFilename());
                            Log.d("fyp", sMsg);
                            tellUI(sMsg);
                            pixelClean(workData);
                            tellUI("Pixels Cleaned.\n");
                            Log.d("fyp", "Pixels Cleaned:\n");
                            Log.v("fyp", WorkingData.toString(workData.result, Integer.toString(plane)));


                        }
                    }
                }

                return workData;

            }

            private void pixelClean(WorkingData workData) throws Exception {
                // Process pixels to create new file
                // New pixel = (RAW - Bias) / Flat = new pixel
                int p=0, x=0, y =0;

                try {

                    for (p = 0; p < workData.fitsPixels.length; p++) {
                        for (x = 0; x < workData.boxsize; x++) {
                            for (y = 0; y < workData.boxsize; y++) {
                                workData.result[p][x][y] = (workData.fitsPixels[p][x][y] - workData.biasPixels[0][x][y]) / workData.flatPixels[0][x][y];
                                // Log.d("fyp", String.format("p%d, x%d, y%d, %.10f", p,x,y,workData.result[p][x][y]));
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new Exception(String.format("Error cleaning pixels at pixel p%d x%d y%d: %s",
                            p, x, y, e.getMessage()));
                }

            }

            private void doWork(ControlMessage ctlMsg) {
                Log.d("fyp", "-> doWork()...");

                // For each Work message
                // For each config star (dummy values initially)For each work message (ie FITS file)
                // For each plane in the FITS
                // Download FITS pixels for the star's X, Y, box
                // Pixel clean()
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

            private void ack(ArrayList<WorkMessage> wrkMsgs, Channel channelWRK) {
                // tell RabbitMQ that the message has been processed
                try {
                    for (WorkMessage wm : wrkMsgs) {
                        channelWRK.basicAck(wm.getDeliveryTag(), false); // send ack to RabbitMQ
                    }
                } catch (IOException e) {
                    System.out.println("Error acking: "+e.getMessage());
                }
            }

        });
        subscribeThread.start();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //    publishThread.interrupt();
        subscribeThread.interrupt();
    }

}
