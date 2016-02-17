package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.LoginFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.security.KeyWrapper;
import pl.openpkw.openpkwmobile.security.SecurityECDSA;
import pl.openpkw.openpkwmobile.utils.StringUtils;


public class LoginActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FragmentManager fm = getFragmentManager();
        LoginFragment loginFragment = (LoginFragment) fm.findFragmentByTag(StringUtils.LOGIN_FRAGMENT_TAG);
        if (loginFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.login_fragment_container, new LoginFragment(), StringUtils.LOGIN_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }

        // add spongy castle security provider
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        //generate and wrap ECDSA keys
        generateKeys();
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
        Fragment loginFragment = getFragmentManager().findFragmentByTag(StringUtils.LOGIN_FRAGMENT_TAG);
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
                    ft.add(R.id.login_fragment_container, settingsFragment, StringUtils.SETTINGS_FRAGMENT_TAG);
                    ft.hide(loginFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show(settingsFragment);
                    ft.hide(loginFragment);
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
                    ft.add(R.id.login_fragment_container, aboutFragment , StringUtils.ABOUT_FRAGMENT_TAG);
                    ft.hide(loginFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    fm.executePendingTransactions();
                }
                else
                {
                    ft.show( aboutFragment);
                    ft.hide(loginFragment);
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
            LoginFragment loginFragment = (LoginFragment) fm.findFragmentByTag(StringUtils.LOGIN_FRAGMENT_TAG);
            if (loginFragment != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentSettings);
                ft.show( loginFragment);
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
            LoginFragment loginFragment = (LoginFragment) fm.findFragmentByTag(StringUtils.LOGIN_FRAGMENT_TAG);
            if (loginFragment != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(fragmentAbout);
                ft.show( loginFragment);
                ft.addToBackStack(null);
                ft.commit();
                fm.executePendingTransactions();
            }
        }
        else
        {
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

    public void generateKeys(){
        try {
            SharedPreferences sharedPref = getSharedPreferences(StringUtils.DATA, Context.MODE_PRIVATE);
            KeyWrapper keyWrapper = new KeyWrapper(getApplicationContext(),StringUtils.KEY_ALIAS);
            if(sharedPref.getString(StringUtils.PRIVATE_KEY,null)==null)
            {
                KeyPair keyPair = SecurityECDSA.generateKeys();
                byte [] privateKeyByteArr = keyWrapper.wrapPrivateKey(keyPair.getPrivate());
                byte [] publicKeyByteArr = keyWrapper.wrapPublicKey(keyPair.getPublic());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(StringUtils.PRIVATE_KEY,Base64.encodeToString(privateKeyByteArr,Base64.DEFAULT));
                editor.putString(StringUtils.PUBLIC_KEY, Base64.encodeToString(publicKeyByteArr, Base64.DEFAULT));
                editor.apply();
            }

        } catch (GeneralSecurityException e) {
            Log.e(StringUtils.TAG, "GeneralSecurityException: " + e.getMessage());
        }
    }
}
