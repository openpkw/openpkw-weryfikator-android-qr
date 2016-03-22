package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.concurrent.TimeUnit;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.ScanQrCodeFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

public class ScanQrCodeActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    public static TextView sessionTimerTextView;
    private CountDownTimer sessionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        FragmentManager fm = getFragmentManager();
        ScanQrCodeFragment scanQRFragment = (ScanQrCodeFragment)
                fm.findFragmentByTag(Utils.SCAN_QR_FRAGMENT_TAG );
        if (scanQRFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.scan_qr_fragment_container, new ScanQrCodeFragment(),
                    Utils.SCAN_QR_FRAGMENT_TAG );
            ft.commit();
            fm.executePendingTransactions();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case IntentIntegrator.REQUEST_CODE:
            {
                if (resultCode == RESULT_CANCELED){
                    Log.i(Utils.TAG, "SCAN CANCELED");
                }
                else
                {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    writeQRtoSharedPreferences(Utils.QR,scanResult.getContents());
                    Log.i(Utils.TAG, "SCAN OK");
                    Log.i(Utils.TAG, "QR: "+scanResult.getContents());
                    Toast.makeText(getApplicationContext(),getString(R.string.scan_qr_label_was_scanned)+". "
                            +getString(R.string.scan_qr_label_send)+".", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void writeQRtoSharedPreferences(String key, String qr) {
        SharedPreferences sharedPref = getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,qr);
        editor.apply();
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
        Fragment scanQRFragment = getFragmentManager().findFragmentByTag(Utils.SCAN_QR_FRAGMENT_TAG);
        //hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Display the settings fragment as the main content.
                SettingsFragment settingsFragment = (SettingsFragment)
                        fm.findFragmentByTag(Utils.SETTINGS_FRAGMENT_TAG);
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                    ft.add(R.id.scan_qr_fragment_container, settingsFragment,
                            Utils.SETTINGS_FRAGMENT_TAG);
                    ft.hide(scanQRFragment );
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show(settingsFragment);
                    ft.hide(scanQRFragment );
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                return true;

            case R.id.about_project:
                // Display the about fragment as the main content.
                AboutFragment aboutFragment = (AboutFragment)
                        fm.findFragmentByTag(Utils.ABOUT_FRAGMENT_TAG);
                if (aboutFragment  == null) {
                    aboutFragment  = new AboutFragment();
                    ft.add(R.id.scan_qr_fragment_container, aboutFragment,
                            Utils.ABOUT_FRAGMENT_TAG);
                    ft.hide(scanQRFragment );
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show( aboutFragment);
                    ft.hide(scanQRFragment);
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
            ScanQrCodeFragment scanQRFragment  = (ScanQrCodeFragment)
                    fm.findFragmentByTag(Utils.SCAN_QR_FRAGMENT_TAG);
            if (scanQRFragment  != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentSettings);
                ft.show( scanQRFragment );
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
            ScanQrCodeFragment scanQRFragment = (ScanQrCodeFragment)
                    fm.findFragmentByTag(Utils.SCAN_QR_FRAGMENT_TAG);
            if (scanQRFragment  != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentAbout);
                ft.show( scanQRFragment);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
            }
        }
        else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
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

                FragmentManager fm = getFragmentManager();
                ScanQrCodeFragment scanQRFragment = (ScanQrCodeFragment)
                        fm.findFragmentByTag(Utils.SCAN_QR_FRAGMENT_TAG);
                if(scanQRFragment!=null)
                    scanQRFragment.showSessionTimeoutAlertDialog();
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

    public static long getSessionTimeout()
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
