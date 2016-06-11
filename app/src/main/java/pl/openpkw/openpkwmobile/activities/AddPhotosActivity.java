package pl.openpkw.openpkwmobile.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.AddPhotosFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;
import static pl.openpkw.openpkwmobile.utils.Utils.PERMISSION_REQUEST_CAMERA;
import static pl.openpkw.openpkwmobile.utils.Utils.PERMISSION_WRITE_EXTERNAL_STORAGE;

public class AddPhotosActivity extends AppCompatActivity implements AddPhotosFragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;
    private View addPhotosLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photos);

        addPhotosLayout = findViewById(R.id.add_photos_fragment_container);

        FragmentManager fm = getFragmentManager();
        AddPhotosFragment addPhotosFragment = (AddPhotosFragment) fm.findFragmentByTag(Utils.ADD_PHOTOS_FRAGMENT_TAG);
        if (addPhotosFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.add_photos_fragment_container, new  AddPhotosFragment(), Utils.ADD_PHOTOS_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
        //check access to camera and storage
        checkPermissions();
    }

    private void showProcessInfo() {
        Snackbar snackbar = Snackbar.make(addPhotosLayout,"Krok 8 - Zrób zdjęcia protokołu wyborczego.",
                Snackbar.LENGTH_SHORT);
        snackbar.show();

        TextView snackBarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackBarTextView.setMaxLines(5);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestWriteStoragePermission();
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            showProcessInfo();
        }
    }

    private void requestWriteStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.

            Snackbar snackbar = Snackbar.make(addPhotosLayout,"Zezwól na dostęp do pamięci urządzenia w celu zapisania zdjęć z protokołu wyborczego.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK",new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(AddPhotosActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_WRITE_EXTERNAL_STORAGE);
                }
            });

            snackbar.show();

            TextView snackBarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackBarTextView.setMaxLines(5);

        } else {
            Snackbar.make(addPhotosLayout,
                    "Permission is not available. Requesting write storage permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.

            Snackbar snackbar = Snackbar.make(addPhotosLayout,"Zezwól na dostęp do aparatu w celu zeskanowania kodu QR.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK",new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(AddPhotosActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            });

            snackbar.show();

            TextView snackBarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackBarTextView.setMaxLines(5);

        } else {
            Snackbar.make(addPhotosLayout,
                    "Permission is not available. Requesting camera permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        CountDownTimer delayTimer = new CountDownTimer(1000,2000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                showProcessInfo();
            }
        };

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(addPhotosLayout, "Aplikacja ma zezwolenie na dostęp do aparatu.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                delayTimer.start();
            } else {
                // Permission request was denied.
                Snackbar.make(addPhotosLayout, "Aplikacja nie ma zezwolenie na dostęp do aparatu.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                delayTimer.start();
            }
        }

        if(requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE){
            // Request for write storage permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(addPhotosLayout, "Aplikacja ma zezwolenie na dostęp do pamięci urządzenia.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                // Permission request was denied.
                Snackbar.make(addPhotosLayout, "Aplikacja nie ma zezwolenie na dostęp do pamięci urządzenia.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
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

        if(timer!=null){
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
