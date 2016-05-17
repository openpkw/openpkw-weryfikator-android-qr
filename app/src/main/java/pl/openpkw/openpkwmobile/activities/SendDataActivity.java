package pl.openpkw.openpkwmobile.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.SendDataFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

public class SendDataActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        FragmentManager fm = getFragmentManager();
        SendDataFragment sendDataFragment = (SendDataFragment) fm.findFragmentByTag(Utils.SEND_DATA_FRAGMENT_TAG );
        if (sendDataFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.send_data_fragment_container, new SendDataFragment(), Utils.SEND_DATA_FRAGMENT_TAG );
            ft.commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        //set session timer
        MenuItem timerMenuItem = menu.findItem(R.id.session_timer);
        TextView sessionTimerTextView = (TextView) MenuItemCompat.getActionView(timerMenuItem);
        sessionTimerTextView.setPadding(10, 0, 10, 0);
        sessionTimerTextView.setText(timer.getTimer());
        timer.setTimeTextView(sessionTimerTextView);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            timer.cancel();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.fragment_login_twotaptoexit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);
    }
}
