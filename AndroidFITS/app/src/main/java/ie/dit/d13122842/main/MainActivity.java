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

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import ie.dit.d13122842.config.Config;

public class MainActivity extends AppCompatActivity {

    private class UI {
        public TextView tvCTLHead;
        public TextView tvCTLStatus;
        public TextView tvWRKHead;
        public TextView tvWRKStatus1;
        public TextView tvWRKStatus2;
        public TextView tvWRKStatus3;
        public TextView tvSummary1Label;
        public TextView tvSummary1;
        public TextView tvSummary2Label;
        public TextView tvSummary2;
        public TextView tvError;
        public UI() {

            tvCTLHead = (TextView) findViewById(R.id.tvCTLHead);
            tvCTLStatus = (TextView) findViewById(R.id.tvCTLStatus);
            tvWRKHead = (TextView) findViewById(R.id.tvWRKHead);
            tvWRKStatus1 = (TextView) findViewById(R.id.tvWRKStatus1);
            tvWRKStatus2 = (TextView) findViewById(R.id.tvWRKStatus2);
            tvWRKStatus3 = (TextView) findViewById(R.id.tvWRKStatus3);
            tvSummary1Label = (TextView) findViewById(R.id.tvSummary1Label);
            tvSummary1 = (TextView) findViewById(R.id.tvSummary1);
            tvSummary2Label = (TextView) findViewById(R.id.tvSummary2Label);
            tvSummary2 = (TextView) findViewById(R.id.tvSummary2);
            tvError = (TextView) findViewById(R.id.tvError);

            tvCTLHead.setText("Not running");
            tvCTLStatus.setText("");
            tvWRKHead.setText("No work");
            tvWRKStatus1.setText("");
            tvWRKStatus2.setText("");
            tvWRKStatus3.setText("");
            tvSummary1Label.setText("Work Units Processed:");
            tvSummary1.setText("0");
            tvSummary2Label.setText("Avg Time per Unit:");
            tvSummary2.setText("n/a");
            tvError.setText("");
        }
        public void setText(Enums.UITarget target, String str) {
            switch (target) {
                case CTLHEAD:
                    ui.tvCTLHead.setText(str);
                    break;
                case CTLSTATUS:
                    ui.tvCTLStatus.setText(str);
                    break;
                case WRKHEAD:
                    ui.tvWRKHead.setText(str);
                    break;
                case WRKSTATUS1:
                    ui.tvWRKStatus1.setText(str);
                    break;
                case WRKSTATUS2:
                    ui.tvWRKStatus2.setText(str);
                    break;
                case WRKSTATUS3:
                    ui.tvWRKStatus3.setText(str);
                    break;
                case SUMMARY1LABEL:
                    ui.tvSummary1Label.setText(str);
                    break;
                case SUMMARY1:
                    ui.tvSummary1.setText(str);
                    break;
                case SUMMARY2LABEL:
                    ui.tvSummary2Label.setText(str);
                    break;
                case SUMMARY2:
                    ui.tvSummary2.setText(str);
                    break;
                case ERROR:
                    ui.tvError.setText(str);
                    break;
            }
        }
    }
    UI ui;

    private ConnectionFactory factory;

    Button btnReset;
    TextView tvMain;
    ScrollView scrollView;

    Thread subscribeThread = null;
    //  Thread publishThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ui = new UI();
        History.reset();

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain.setTypeface(Typeface.MONOSPACE);

        try {
            factory = new ConnectionFactory();
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(Config.MQ.QUEUE_URI);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            Log.e("fyp", "-> Error in setupConnectionFactory(): "+e.getMessage());
            e.printStackTrace();
        }

        // Handler to print messages generated within subscribeThread on the UI thread
        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                String message = msg.getData().getString("msg");
//                Date now = new Date();
//                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
//                tvMain.append(ft.format(now) + ' ' + message + '\n');
//                scrollView.post(new Runnable() {
//                    public void run() {
//                        scrollView.fullScroll(View.FOCUS_DOWN);
//                    }
//                });
                String tgt = msg.getData().getString("tgt");
                String str = msg.getData().getString("str");
                if (tgt != null)
                    ui.setText(Enums.UITarget.valueOf(tgt), str);
            }
        };

        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //btnReset.setText("Stop");
                Log.d("fyp", "Reset tapped.");
                tvMain.setText("");
                if (subscribeThread == null) {
                    subscribeThread = new Thread (new ControlClient(incomingMessageHandler, factory));
                    subscribeThread.start();
                } else {
                    tvMain.setText(""); // clear
                }
                //if(running) subscribeThread.interrupt();

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //    publishThread.interrupt();
        subscribeThread.interrupt();
    }

}
