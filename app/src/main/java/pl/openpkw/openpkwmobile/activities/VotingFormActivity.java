package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.fragments.VotingFormFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

public class VotingFormActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    public static TextView sessionTimerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_form);

        FragmentManager fm = getFragmentManager();
        VotingFormFragment votingFormFragment = (VotingFormFragment)
                fm.findFragmentByTag(Utils.VOTING_FORM_FRAGMENT_TAG);
        if (votingFormFragment  == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.voting_form_fragment_container, new VotingFormFragment(),
                    Utils.VOTING_FORM_FRAGMENT_TAG);
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
        sessionTimerTextView.setText(timer.getTimer());
        timer.setTimeTextView(sessionTimerTextView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        android.app.FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction ft = fm.beginTransaction();
        Fragment votingFormFragment = getFragmentManager().findFragmentByTag(Utils.VOTING_FORM_FRAGMENT_TAG);
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
                    ft.add(R.id.voting_form_fragment_container, settingsFragment,
                            Utils.SETTINGS_FRAGMENT_TAG);
                    ft.hide(votingFormFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }
                else
                {
                    ft.show(settingsFragment);
                    ft.hide(votingFormFragment);
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
                    ft.add(R.id.voting_form_fragment_container, aboutFragment,
                            Utils.ABOUT_FRAGMENT_TAG);
                    ft.hide(votingFormFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show( aboutFragment);
                    ft.hide(votingFormFragment );
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
            VotingFormFragment votingFormFragment  = (VotingFormFragment)
                    fm.findFragmentByTag(Utils.VOTING_FORM_FRAGMENT_TAG);
            if (votingFormFragment  != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentSettings);
                ft.show( votingFormFragment);
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
            VotingFormFragment votingFormFragment = ( VotingFormFragment)
                    fm.findFragmentByTag(Utils.VOTING_FORM_FRAGMENT_TAG);
            if (votingFormFragment  != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentAbout);
                ft.show(votingFormFragment);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
            }
        }
        else {

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

    protected void onDestroy() {
        super.onDestroy();
    }
}
