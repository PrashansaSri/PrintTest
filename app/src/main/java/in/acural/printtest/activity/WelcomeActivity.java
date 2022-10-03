package in.acural.printtest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;

import in.acural.printtest.MainActivity;
import in.acural.printtest.R;


/**
 *create by Rupendra Srivastava at 13/1/2019
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final int GO_MAIN = 1000;
    private static final long SPLASH_DELAY_MILLIS = 1501;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        init();
    }

    /**
     * init
     */
    private void init() {handler.sendEmptyMessageDelayed(GO_MAIN, SPLASH_DELAY_MILLIS);}

    /**
     * Handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GO_MAIN:
                    goMain();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * goMain
     */
    private void goMain() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        WelcomeActivity.this.startActivity(intent);
        WelcomeActivity.this.finish();
    }


}
