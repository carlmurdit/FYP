package ie.dit.d13122842.main;

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

        // create a method to allow UI updates from the subscribeThread
        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                // TextView tv = (TextView) findViewById(R.id.tvMain);
                Date now = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
                tvMain.append(ft.format(now) + ' ' + message + '\n');

                //scrollView.fullScroll(View.FOCUS_DOWN);
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
                        final Channel channelWRK = connection.createChannel(); // channels not thread-safe
                        channelWRK.queueDeclare(QUEUE_NAME_WRK, true, false, false, null);
                        channelWRK.basicQos(0); // prefetch count unlimited (because pulled)

                        // create channel for Control messages
                        final Channel channelCTL = connection.createChannel();
                        channelCTL.queueDeclare(QUEUE_NAME_CTL, true, false, false, null);
                        channelCTL.basicQos(1); // prefetch count

                        QueueingConsumer consumer = new QueueingConsumer(channelWRK);
                        channelCTL.basicConsume(QUEUE_NAME_CTL, AUTOACK_OFF, consumer); //autoAck false

                        // Process received messages
                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                            // create objects for the messages we will get
                            ControlMessage ctlMsg = null;
                            ArrayList<WorkMessage> wrkMsgs = new ArrayList<WorkMessage>();

                            String messageCTL = new String(delivery.getBody());
                            Log.d("", "-> [r] " + messageCTL);

                            ctlMsg = parser.parseControlMessage(messageCTL, delivery.getEnvelope().getDeliveryTag());
                            Log.d("", "-> ControlMessage object created:\n" + ctlMsg.toString());
                            tellUI("-> Control message received!\n" + ctlMsg.toString()+"\n");

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

                            // Get the config file set in the control message.
                            // Parse each line into Stars.
                            // Get pixel boxes around each Star from Flat and Bias
                            ArrayList<Star> stars = getConfig(ctlMsg);

                            // do the rest...
                            doWork(ctlMsg, stars);




                            //ack all messages
                            ack(ctlMsg, channelCTL, wrkMsgs, channelWRK);

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

            private ArrayList<Star> getConfig(ControlMessage ctlMsg) throws Exception {
                Log.d("", "-> getConfig()...");

                // Download the config (star list) file and read the stars
                final ArrayList<Star> stars = new ArrayList<Star>();

                tellUI("Requesting Config (POST to " + ctlMsg.getAPI_Server_URL() + ")");

                FormPoster poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                poster.add("action", "getfile");
                poster.add("filename", ctlMsg.getConfig_Filename()); // add POST variables
                String configContents = poster.post();

                // parse the contents into stars
                parser.parseConfig(configContents, stars);

                // display them
                for (int i=0; i<stars.size(); i++) {
                    Star star = stars.get(i);
                    tellUI(String.format("Star %d: X %d, Y %d, Box width %d",
                            i, star.x, star.y, star.boxwidth));
                }

                if (stars.size() == 0) {
                    throw new Exception("No stars found in Config: "+configContents);
                }

                WorkingData workData = null;

                // For each star in the config, download boxes from flat and bias
               for (int i=0; i<stars.size(); i++) {
                //for (int i=0; i<1; i++) {
                    Star star = stars.get(i);

                    workData = new WorkingData(star.boxwidth);

                    int x1 = star.x - star.boxwidth/2;
                    int y1 = star.y - star.boxwidth/2;
                    int x2 = star.x + star.boxwidth/2;
                    int y2 = star.y + star.boxwidth/2;
                    // see 5.2 FITS File Access Routines, p 39 of 186 in cfitsio user ref guide
                    String box = String.format("[%d:%d,%d:%d]", x1, y1, x2, y2);
                    // todo - remove test data
                    workData = new WorkingData(10);
                    box = "[101:110,201:210]";
                    star.boxwidth = 10;
                    // end todo

                    poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                    poster.add("action", "getbox");
                    poster.add("box", box);
                    poster.add("filename", ctlMsg.getFlat_Filename()); // add POST variables
                    poster.add("plane","1");
                    tellUI(String.format("STAR %d: Requesting FLAT X %d, Y %d, Box width %d, %s\n",
                            i, star.x, star.y, star.boxwidth, box));
                    String flatResponse = poster.post();

                    // populate an array from the returned Flat data
                    //double[][][] flatPixels = new double[1][star.boxwidth][star.boxwidth];
                    parser.parsePixels(flatResponse, workData.flatPixels);
                    tellUI("Flat Pixels:\n" + WorkingData.toString(workData.flatPixels));

                    // repeat for Bias
                    // todo - remove test data
                    box = "[151:160,251:260]";
                    // end todo
                    poster = new FormPoster(ctlMsg.getAPI_Server_URL());
                    poster.add("action", "getbox");
                    poster.add("box", box);
                    poster.add("filename", ctlMsg.getBias_Filename()); // add POST variables
                    poster.add("plane","1");
                    tellUI(String.format("STAR %d: Requesting BIAS X %d, Y %d, Box width %d, %s\n",
                            i, star.x, star.y, star.boxwidth, box));
                    String biasResponse = poster.post();

                    // populate an array from the returned Bias data
                    // double[][][] biasPixels = new double[1][star.boxwidth][star.boxwidth];
                    parser.parsePixels(biasResponse, workData.biasPixels);
                    tellUI("Bias Pixels:\n" + WorkingData.toString(workData.biasPixels));

                }

                return stars;

            }

            private void doWork(ControlMessage ctlMsg, ArrayList<Star> stars) {
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

            private void ack(ControlMessage ctlMsg, Channel channelCTL, ArrayList<WorkMessage> wrkMsgs, Channel channelWRK) {
                // tell RabbitMQ that the message has been processed
                try {
                    channelCTL.basicAck(ctlMsg.getDeliveryTag(), false);
                    for (WorkMessage wm : wrkMsgs) {
                        channelWRK.basicAck(wm.getDeliveryTag(), false);
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
