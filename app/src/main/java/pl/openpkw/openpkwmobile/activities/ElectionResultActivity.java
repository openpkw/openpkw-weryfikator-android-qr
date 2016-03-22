package pl.openpkw.openpkwmobile.activities;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.ElectionResultFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

public class ElectionResultActivity extends AppCompatActivity {

    private TextView sessionTimerTextView;
    private CountDownTimer sessionTimer;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_result);

        FragmentManager fm = getFragmentManager();
        ElectionResultFragment electionResultFragment = (ElectionResultFragment) fm.findFragmentByTag(Utils.ELECTION_RESULT_FRAGMENT_TAG);
        if (electionResultFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.election_result_fragment_container, new  ElectionResultFragment(), Utils.ELECTION_RESULT_FRAGMENT_TAG);
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
        sessionTimerTextView = (TextView) MenuItemCompat.getActionView(timerMenuItem);
        sessionTimerTextView.setPadding(10, 0, 10, 0);
        Bundle extras = getIntent().getExtras();
        if(extras!=null)
            startSessionTimer(extras.getLong(Utils.TIMEOUT, Utils.SESSION_TIMER), 1000);
        else
            startSessionTimer(Utils.SESSION_TIMER, 1000);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment electionResultFragment = getFragmentManager().findFragmentByTag(Utils.ELECTION_RESULT_FRAGMENT_TAG);
        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        //hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Display the settings fragment as the main content.
                SettingsFragment settingsFragment = (SettingsFragment) fm.findFragmentByTag(Utils.SETTINGS_FRAGMENT_TAG);
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                    ft.add(R.id.election_result_fragment_container, settingsFragment, Utils.SETTINGS_FRAGMENT_TAG);
                    ft.hide(electionResultFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show(settingsFragment);
                    ft.hide(electionResultFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                return true;

            case R.id.about_project:
                // Display the about fragment as the main content.
                AboutFragment aboutFragment = (AboutFragment) fm.findFragmentByTag(Utils.ABOUT_FRAGMENT_TAG);
                if (aboutFragment  == null) {
                    aboutFragment  = new AboutFragment();
                    ft.add(R.id.election_result_fragment_container, aboutFragment , Utils.ABOUT_FRAGMENT_TAG);
                    ft.hide(electionResultFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show( aboutFragment);
                    ft.hide(electionResultFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentSettings = getFragmentManager().findFragmentByTag(Utils.SETTINGS_FRAGMENT_TAG);
        Fragment fragmentAbout = getFragmentManager().findFragmentByTag(Utils.ABOUT_FRAGMENT_TAG);
        if(fragmentSettings!=null && fragmentSettings.isVisible())
        {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();

            FragmentManager fm = getFragmentManager();
            ElectionResultFragment electionResultFragment = (ElectionResultFragment)
                    fm.findFragmentByTag(Utils.ELECTION_RESULT_FRAGMENT_TAG);
            if (electionResultFragment != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentSettings);
                ft.show( electionResultFragment);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
            }

        }
        else if(fragmentAbout !=null && fragmentAbout .isVisible()) {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();

            FragmentManager fm = getFragmentManager();
            ElectionResultFragment electionResultFragment = (ElectionResultFragment)
                    fm.findFragmentByTag(Utils.ELECTION_RESULT_FRAGMENT_TAG);
            if (electionResultFragment!= null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentAbout);
                ft.show(electionResultFragment);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
            }
        }
        else {

            Intent scanIntent = new Intent(ElectionResultActivity.this, ScanQrCodeActivity.class);
            scanIntent.putExtra(Utils.TIMEOUT,getSessionTimeout());
            startActivity(scanIntent);
            finish();
        }
    }

    private void startSessionTimer(long duration, long interval) {

        sessionTimer = new CountDownTimer(duration, interval) {
            String minStr;
            String secStr;
            String timer;
            long min;
            long sec;

            @Override
            public void onFinish() {
                sessionTimerTextView.setText(R.string.session_timer_finish);
                ElectionResultFragment electionResultFragment = (ElectionResultFragment)getFragmentManager().findFragmentByTag(Utils.ELECTION_RESULT_FRAGMENT_TAG);
                if(electionResultFragment!=null)
                    electionResultFragment.showSessionTimeoutAlertDialog();
            }

            @Override
            public void onTick(long millisecondsLeft) {

                min = (TimeUnit.MILLISECONDS.toMinutes(millisecondsLeft) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisecondsLeft)));

                sec = TimeUnit.MILLISECONDS.toSeconds(millisecondsLeft)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisecondsLeft));

                if(min<10) {
                    minStr = "0" + min;
                } else{
                    minStr = "" + min;
                }

                if(sec<10) {
                    secStr = "0" + sec;
                }else{
                    secStr = "" + sec;
                }

                timer = minStr + ":" + secStr;
                sessionTimerTextView.setText(timer);
            }
        };

        sessionTimer.start();
    }

    protected void onDestroy() {
        super.onDestroy();
        sessionTimer.cancel();
    }

    private long getSessionTimeout()
    {
        int min = 0;
        int sec = 0;
        if(sessionTimerTextView!=null) {
            String timerStr = sessionTimerTextView.getText().toString();
            String minStr = timerStr.substring(0, 2);
            String secStr = timerStr.substring(3, 5);
            min = Integer.valueOf(minStr);
            sec = Integer.valueOf(secStr);
        }
        return (min*60*1000+sec*1000);
    }
}
