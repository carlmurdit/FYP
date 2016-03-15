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
import java.text.SimpleDateFormat;
import java.util.Date;

import ie.dit.d13122842.config.Config;

public class MainActivity extends AppCompatActivity {

    private final String QUEUE_NAME_CTL = "control_queue";
    private final String QUEUE_NAME_WRK = "work_queue";
    private final String QUEUE_NAME_RLT = "result_queue";
    private final boolean AUTOACK_ON = true;
    private final boolean AUTOACK_OFF = false;
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
                if (subscribeThread == null) {
                    subscribeThread = new Thread (new Cleaner(incomingMessageHandler, factory));
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
