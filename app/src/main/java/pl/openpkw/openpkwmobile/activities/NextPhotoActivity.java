package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.camera.CameraActivity;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.NextPhotoFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

public class NextPhotoActivity extends AppCompatActivity implements NextPhotoFragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;
    private View nextPhotoLayout;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_photo);

        nextPhotoLayout = findViewById(R.id.next_photo_fragment_container);

        //get path to photo
        Bundle extras = getIntent().getExtras();
        mCurrentPhotoPath = extras.getString(Utils.PATH_TO_PHOTO, null);

        FragmentManager fm = getFragmentManager();
        NextPhotoFragment nextPhotoFragment = (NextPhotoFragment) fm.findFragmentByTag(Utils.NEXT_PHOTO_FRAGMENT_TAG);
        if (nextPhotoFragment == null) {

            nextPhotoFragment = new NextPhotoFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Utils.PATH_TO_PHOTO, mCurrentPhotoPath);
            nextPhotoFragment.setArguments(bundle);

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.next_photo_fragment_container, nextPhotoFragment, Utils.NEXT_PHOTO_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }

        //set title and subtitle to action bar
        //set title and subtitle to action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            final SpannableString spannableString = new SpannableString("Krok 8 z 9\nWykonaj zdjęcia każdej strony protokołu wyborczego");
            spannableString.setSpan(new RelativeSizeSpan(1.2f),0,"Krok 8 z 9".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar_title_layout);
            ((TextView) findViewById(R.id.action_bar_title)).setText(spannableString);
        }

        showInfo();
    }

    private void showInfo(){
        Snackbar snackbar = Snackbar.make(nextPhotoLayout,"Czy zdjęcie jest wyraźne i dobrze wykadrowane?",
                Snackbar.LENGTH_LONG).
                setAction("PONÓW",new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //delete incorrect photo
                if(mCurrentPhotoPath!=null){
                    File file  = new File(mCurrentPhotoPath);
                    boolean isDeleted = file.delete();
                }

                Intent cameraIntent = new Intent(NextPhotoActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
                finish();
            }
        });
        // Changing message text color
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.setDuration(4000);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        // Changing action button text color
        TextView snackBarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackBarTextView.setMaxLines(5);
        snackBarTextView.setTypeface(Typeface.DEFAULT_BOLD);
        snackBarTextView.setTextColor(Color.WHITE);
        snackBarTextView.setTextSize(16);
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
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
    public boolean onOptionsItemSelected(MenuItem item){
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
        //begin transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Create new fragment and transaction
                Fragment settingsFragment = new SettingsFragment();
                transaction.replace(android.R.id.content, settingsFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            case R.id.about_project:
                Fragment aboutFragment = new AboutFragment();
                transaction.replace(android.R.id.content, aboutFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();
            //show main fragment
            getFragmentManager().popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
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
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
