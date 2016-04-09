package pl.openpkw.openpkwmobile.activities;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.ElectionResultFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.StringUtils;

public class ElectionResultActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_result);

        FragmentManager fm = getFragmentManager();
        ElectionResultFragment electionResultFragment = (ElectionResultFragment) fm.findFragmentByTag(StringUtils.ELECTION_RESULT_FRAGMENT_TAG);
        if (electionResultFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.election_result_fragment_container, new  ElectionResultFragment(), StringUtils.ELECTION_RESULT_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment electionResultFragment = getFragmentManager().findFragmentByTag(StringUtils.ELECTION_RESULT_FRAGMENT_TAG);
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
                SettingsFragment settingsFragment = (SettingsFragment) fm.findFragmentByTag(StringUtils.SETTINGS_FRAGMENT_TAG);
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                    ft.add(R.id.election_result_fragment_container, settingsFragment, StringUtils.SETTINGS_FRAGMENT_TAG);
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
                AboutFragment aboutFragment = (AboutFragment) fm.findFragmentByTag(StringUtils.ABOUT_FRAGMENT_TAG);
                if (aboutFragment  == null) {
                    aboutFragment  = new AboutFragment();
                    ft.add(R.id.election_result_fragment_container, aboutFragment , StringUtils.ABOUT_FRAGMENT_TAG);
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
        Fragment fragmentSettings = getFragmentManager().findFragmentByTag(StringUtils.SETTINGS_FRAGMENT_TAG);
        Fragment fragmentAbout = getFragmentManager().findFragmentByTag(StringUtils.ABOUT_FRAGMENT_TAG);
        if(fragmentSettings!=null && fragmentSettings.isVisible())
        {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();

            FragmentManager fm = getFragmentManager();
            ElectionResultFragment electionResultFragment = (ElectionResultFragment)
                    fm.findFragmentByTag(StringUtils.ELECTION_RESULT_FRAGMENT_TAG);
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
                    fm.findFragmentByTag(StringUtils.ELECTION_RESULT_FRAGMENT_TAG);
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
            startActivity(scanIntent);
            finish();
        }
    }
}
