package pl.openpkw.openpkwmobile.camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.RetryPhotoActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

public class CameraActivity extends AppCompatActivity implements Camera2Fragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (null == savedInstanceState) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2Fragment.newInstance())
                        .commit();
            }else{
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, CameraFragment.newInstance())
                        .commit();
            }
        }

        //set title and subtitle to action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("Krok 8 z 9");
            actionBar.setSubtitle("Wykonaj zdjęcia protokołu wyborczego");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_camera, menu);
        //set session timer
        MenuItem timerMenuItem = menu.findItem(R.id.session_timer);
        TextView sessionTimerTextView = (TextView) MenuItemCompat.getActionView(timerMenuItem);
        sessionTimerTextView.setPadding(10, 0, 10, 0);
        if(timer!=null) {
            sessionTimerTextView.setText(timer.getTimer());
            timer.setTimeTextView(sessionTimerTextView);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();

            if(timer!=null)
                timer.cancel();
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

    @Override
    public void onFragmentInteraction(String mCurrentPhotoPath) {
        Intent retryPhotoIntent = new Intent(this, RetryPhotoActivity.class);
        Log.e(Utils.TAG,"PATH TO PICTURE "+mCurrentPhotoPath);
        retryPhotoIntent.putExtra(Utils.PATH_TO_PHOTO, mCurrentPhotoPath);
        startActivity(retryPhotoIntent);
        finish();
    }
}
