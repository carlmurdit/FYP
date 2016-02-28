package ie.dit.d13122842.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;

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
    TextView tvMain;
    ScrollView scrollView;
    Thread subscribeThread;
    //  Thread publishThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setTypeface(Typeface.MONOSPACE);

        tvMain.setText(tvMain.getText() + "\nStarted!\n");
        Log.d("", "->  Started.");

        try {
            factory = new ConnectionFactory();
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(queueURI);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            Log.e("", "-> Error in setupConnectionFactory(): "+e.getMessage());
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
        subscribe(incomingMessageHandler);

    }

    void subscribe(final Handler handler) {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // create a connection
                        Connection connection = factory.newConnection();
                        // create channel for Work messages
                        final Channel channelWRK = connection.createChannel();
                        channelWRK.queueDeclare(QUEUE_NAME_WRK, true, false, false, null);
                        channelWRK.basicQos(0); // prefetch count unlimited (because pulled)

                        // Create channel for Control messages
                        final Channel channelCTL = connection.createChannel();
                        channelCTL.queueDeclare(QUEUE_NAME_CTL, true, false, false, null);
                        channelCTL.basicQos(1); // prefetch count

                        // todo: should this pass channelCTL?
                        QueueingConsumer consumer = new QueueingConsumer(channelCTL);
                        channelCTL.basicConsume(QUEUE_NAME_CTL, AUTOACK_OFF, consumer);

                        while (true) {
                            // Process received Control messages
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                            // todo: Cancel Subscription - we don't want more Control Messages
                            // channelCTL.basicCancel(consumerTag);

                            // create objects for the messages we will get
                            ControlMessage ctlMsg = null;
                            ArrayList<WorkMessage> wrkMsgs = new ArrayList<WorkMessage>();

                            String messageCTL = new String(delivery.getBody());
                            Log.d("", "-> [r] " + messageCTL);

                            ctlMsg = parser.parseControlMessage(messageCTL, delivery.getEnvelope().getDeliveryTag());
                            Log.d("", "-> ControlMessage object created:\n" + ctlMsg.toString());
                            tellUI("-> Control message received!\n" + ctlMsg.toString() + "\n");

                            channelCTL.basicAck(ctlMsg.getDeliveryTag(), true);

                            // tell rabbitMQ to requeue the message
                            // reject(ctlMsg, channelCTL);

                            // get the WORK messages
                            for (int i=1; i<=WORK_PER_CTRL; i++) {
                                GetResponse response = channelWRK.basicGet(QUEUE_NAME_WRK, AUTOACK_OFF);
                                if (response == null) {
                                    tellUI("-> No WRK msg found.");
                                    Log.d("", "\t-> No WRK msg found.\n");
                                } else {
                                    String messageWRK = new String(response.getBody(), "UTF-8");
                                    Log.d("", "-> [r] " + messageCTL);
                                    WorkMessage wrkMsg = parser.parseWorkMessage(messageWRK, response.getEnvelope().getDeliveryTag());
                                    wrkMsgs.add(wrkMsg);
                                    Log.d("", "-> WorkMessage object created:\n" + wrkMsg.toString());
                                    tellUI("-> Work message retrieved!\n" + wrkMsg.toString()+"\n");
                                }
                            }

                            WorkingData workData = getPixels(ctlMsg, wrkMsgs);

                            // do the rest...
                            doWork(ctlMsg);

                            //ack all work messages
                            ack(wrkMsgs, channelWRK);

                            tellUI("All done!");

                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        Log.d("", "-->  Connection broken: " + e1.getClass().getName() + "\n" + e1.getMessage());
                        try {
                            Thread.sleep(4000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
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
                Log.d("", "-> getPixels()...");

                // Download the config (star list) file and read the stars
                final ArrayList<Star> stars = new ArrayList<Star>();

                tellUI("Requesting Config (POST to " + ctlMsg.getAPI_Server_URL() + ")...");

                FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                poster.add("action", "getfile");
                poster.add("filename", ctlMsg.getConfig_Filename()); // add POST variables
                String configContents = poster.post();

                // parse the contents into stars
                parser.parseConfig(configContents, stars);

                // display them
                for (int i=0; i<stars.size(); i++) {
                    Star star = stars.get(i);
                    tellUI(String.format("Star %d: x%d, y%d, boxwidth %d",
                            i, star.x, star.y, star.boxwidth));
                }

                if (stars.size() == 0) {
                    throw new Exception("No stars found in Config: "+configContents);
                }

                // todo: Can different stars have different box sizes?
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

                    poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                    poster.add("action", "getbox");
                    poster.add("box", box);
                    poster.add("filename", ctlMsg.getFlat_Filename()); // add POST variables
                    poster.add("plane", "1");
                    tellUI(String.format("STAR %d: Requesting X %d, Y %d, Box width %d, %s from %s...",
                            i, star.x, star.y, star.boxwidth, box, ctlMsg.getFlat_Filename()));
                    String flatResponse = poster.post();

                    // populate an array from the returned Flat data
                    parser.parsePixels(flatResponse, workData.flatPixels);
                    tellUI("Flat Pixels:\n" + WorkingData.toString(workData.flatPixels,"-"));

                    // repeat for Bias
                    // todo - remove test data
                    // box = "[151:160,251:260]";
                    // end todo
                    poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                    poster.add("action", "getbox");
                    poster.add("box", box);
                    poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
                    poster.add("plane","1");
                    tellUI(String.format("STAR %d: Requesting X %d, Y %d, Box width %d, %s from %s...",
                            i, star.x, star.y, star.boxwidth, box, ctlMsg.getBias_Filename()));
                    String biasResponse = poster.post();

                    // populate an array from the returned Bias data
                    parser.parsePixels(biasResponse, workData.biasPixels);
                    tellUI("Bias Pixels:\n" + WorkingData.toString(workData.biasPixels,"-"));

                    for (WorkMessage wm : wrkMsgs) {
                        for (int plane = 1; plane <= 2; plane++) {
//                        for (int plane = 1; plane <= wm.getPlanes(); plane++) {

                            // todo - remove test data
//                            box = "[101:110,201:210]";
                            // end todo
                            poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                            poster.add("action", "getbox");
                            poster.add("box", box);
                            poster.add("filename", wm.getFilename()); // add POST variables
                            poster.add("plane", Integer.toString(plane));
                            tellUI(String.format("STAR %d: Requesting plane %d, X %d, Y %d, Box width %d, %s from %s...",
                                    i, plane, star.x, star.y, star.boxwidth, box, wm.getFilename()));
                            String fitsResponse = poster.post();

                            // populate an array from the returned Fits data
                            //todo - don't overwrite last plane before processing it
                            parser.parsePixels(fitsResponse, workData.fitsPixels);
                            tellUI("FITS Pixels:\n" + WorkingData.toString(workData.fitsPixels,Integer.toString(plane)));

                            pixelClean(workData);
                            tellUI(String.format("CLEANED PIXELS:\n " +
                                    "STAR %d, plane %d, X %d, Y %d, Box width %d, %s from %s:\n %s",
                                    i, plane, star.x, star.y, star.boxwidth, box, wm.getFilename(),
                                    WorkingData.toString(workData.result, Integer.toString(plane))));

                        }
                    }
                }

                return workData;

            }

            private void pixelClean(WorkingData workData) {
                // Process pixels to create new file
                // New pixel = (RAW - Bias) / Flat = new pixel

                try {

                    for (int p = 0; p < workData.fitsPixels.length; p++) {
                        for (int x = 0; x < workData.boxsize; x++) {
                            for (int y = 0; y < workData.boxsize; y++) {
                                workData.result[p][x][y] = (workData.fitsPixels[p][x][y] - workData.biasPixels[0][x][y]) / workData.flatPixels[0][x][y];
                                // tellUI(String.format("p%d, x%d, y%d, %.10f", p,x,y,workData.result[p][x][y]));
                            }
                        }
                    }

                } catch (Exception e) {
                    tellUI("Error cleaning pixels: "+e.getMessage());
                }

            }

            private void doWork(ControlMessage ctlMsg) {
                Log.d("", "-> doWork()...");

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
        Log.d("", "-->   subscribeThread started");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //    publishThread.interrupt();
        subscribeThread.interrupt();
    }

}
