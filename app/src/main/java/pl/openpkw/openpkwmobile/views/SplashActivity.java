package pl.openpkw.openpkwmobile.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.LoginActivity;
import pl.openpkw.openpkwmobile.fragments.SplashFragment;

/**
 * Created by Wojciech Radzioch on 2015-04-21.
 */
public class SplashActivity extends FragmentActivity {

    public static final int SPLASH_TIME = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SplashFragment())
                    .commit();
        }

        Thread splashThread = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        };
        splashThread.start();
    }
}