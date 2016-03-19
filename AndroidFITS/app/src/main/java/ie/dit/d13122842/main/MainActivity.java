package ie.dit.d13122842.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import ie.dit.d13122842.config.Config;

public class MainActivity extends AppCompatActivity {

    private class UI {
        private TextView tvCTLHead;
        private TextView tvCTLStatus;
        private TextView tvWRKHead;
        private ProgressBar pgbWorkUnit;
        private TextView tvWRKStatus1;
        private TextView tvWRKStatus2;
        private TextView tvWRKStatus3;
        private TextView tvSummary1Label;
        private TextView tvSummary1;
        private TextView tvSummary2Label;
        private TextView tvSummary2;
        private TextView tvError;
        public UI() {
            tvCTLHead = (TextView) findViewById(R.id.tvCTLHead);
            tvCTLStatus = (TextView) findViewById(R.id.tvCTLStatus);
            tvWRKHead = (TextView) findViewById(R.id.tvWRKHead);
            pgbWorkUnit = (ProgressBar) findViewById(R.id.pgbWorkUnit);
            tvWRKStatus1 = (TextView) findViewById(R.id.tvWRKStatus1);
            tvWRKStatus2 = (TextView) findViewById(R.id.tvWRKStatus2);
            tvWRKStatus3 = (TextView) findViewById(R.id.tvWRKStatus3);
            tvSummary1Label = (TextView) findViewById(R.id.tvSummary1Label);
            tvSummary1 = (TextView) findViewById(R.id.tvSummary1);
            tvSummary2Label = (TextView) findViewById(R.id.tvSummary2Label);
            tvSummary2 = (TextView) findViewById(R.id.tvSummary2);
            tvError = (TextView) findViewById(R.id.tvError);
            resetAll();
        }
        public void resetAll(){
            tvCTLHead.setText("Not running");
            tvCTLStatus.setText("");
            tvWRKHead.setText("No work");
            pgbWorkUnit.setProgress(0);
            tvWRKStatus1.setText("");
            tvWRKStatus2.setText("");
            tvWRKStatus3.setText("");
            tvSummary1Label.setText("Work Units Processed:");
            tvSummary1.setText("0");
            tvSummary2Label.setText("Avg Time per Unit:");
            tvSummary2.setText("n/a");
            tvError.setText("");
            History.reset();
        }
        public void resetSummary() {
            tvSummary1.setText("0");
            tvSummary2.setText("n/a");
        }
        public void setValue(Enums.UITarget target, String str, int num) {
            switch (target) {
                case CTL_HEAD:
                    tvCTLHead.setText(str);
                    break;
                case CTL_STATUS:
                    tvCTLStatus.setText(str);
                    break;
                case WRK_HEAD:
                    tvWRKHead.setText(str);
                    break;
                case WRK_STATUS_1:
                    tvWRKStatus1.setText(str);
                    break;
                case WRK_PROGRESS_MAX:
                    pgbWorkUnit.setMax(num);
                    break;
                case WRK_PROGRESS_NEXT:
                    pgbWorkUnit.incrementProgressBy(1);
                    break;
                case WRK_PROGRESS_RESET:
                    pgbWorkUnit.setProgress(0);
                    break;
                case WRK_STATUS_2:
                    tvWRKStatus2.setText(str);
                    break;
                case WRK_STATUS_3:
                    tvWRKStatus3.setText(str);
                    break;
                case SUMMARY_1_LABEL:
                    tvSummary1Label.setText(str);
                    break;
                case SUMMARY_1:
                    tvSummary1.setText(str);
                    break;
                case SUMMARY_2_LABEL:
                    tvSummary2Label.setText(str);
                    break;
                case SUMMARY_2:
                    tvSummary2.setText(str);
                    break;
                case ERROR:
                    tvError.setText(str);
                    break;
                case RESETALL:
                    resetAll();
                    break;
            }
        }
    }
    UI ui;

    private ConnectionFactory factory;

    Button btnStartStop;
    Button btnResetSummary;
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
                int num = msg.getData().getInt("num");
                if (tgt != null)
                    ui.setValue(Enums.UITarget.valueOf(tgt), str, num);
            }
        };

        btnStartStop = (Button) findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("fyp", "Reset tapped.");
                tvMain.setText("");
                if (subscribeThread == null) {
                    subscribeThread = new Thread(new ControlClient(incomingMessageHandler, factory));
                    subscribeThread.start();
                    btnStartStop.setText("Stop");
                } else {
                    tvMain.setText(""); // clear
                    subscribeThread.interrupt();
                    btnStartStop.setText("Start");
                    subscribeThread = null;
                }
            }
        });

        btnResetSummary = (Button) findViewById(R.id.btnResetSummary);
        btnResetSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                History.reset();
                ui.resetSummary();
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
