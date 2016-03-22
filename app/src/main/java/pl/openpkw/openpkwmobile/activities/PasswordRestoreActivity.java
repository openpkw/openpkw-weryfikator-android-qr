package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
import pl.openpkw.openpkwmobile.fragments.PasswordRestoreFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

public class PasswordRestoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestore);

        FragmentManager fm = getFragmentManager();
        PasswordRestoreFragment passwordRestoreFragment = (PasswordRestoreFragment)
                fm.findFragmentByTag(Utils.PRESTORE_FRAGMENT_TAG);

        if (passwordRestoreFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.prestore_fragment_container, new PasswordRestoreFragment(),
                    Utils.PRESTORE_FRAGMENT_TAG);
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
        Fragment passwordRestoreFragment = getFragmentManager().findFragmentByTag(Utils.PRESTORE_FRAGMENT_TAG);
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
                    ft.add(R.id.prestore_fragment_container, settingsFragment,
                            Utils.SETTINGS_FRAGMENT_TAG);
                    ft.hide(passwordRestoreFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show(settingsFragment);
                    ft.hide(passwordRestoreFragment);
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
                    ft.add(R.id.prestore_fragment_container, aboutFragment ,
                            Utils.ABOUT_FRAGMENT_TAG);
                    ft.hide(passwordRestoreFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show( aboutFragment);
                    ft.hide(passwordRestoreFragment);
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
            PasswordRestoreFragment passwordRestoreFragment = (PasswordRestoreFragment)
                    fm.findFragmentByTag(Utils.PRESTORE_FRAGMENT_TAG);
            if (passwordRestoreFragment != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentSettings);
                ft.show( passwordRestoreFragment);
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
            PasswordRestoreFragment passwordRestoreFragment = (PasswordRestoreFragment)
                    fm.findFragmentByTag(Utils.PRESTORE_FRAGMENT_TAG);
            if (passwordRestoreFragment != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentAbout);
                ft.show(passwordRestoreFragment);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
            }
        }
        else {
          finish();
        }
    }
}
