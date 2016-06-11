package pl.openpkw.openpkwmobile.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.ScanQrCodeFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.loaders.CandidatesDataLoader;
import pl.openpkw.openpkwmobile.loaders.ElectionCommitteesDataLoader;
import pl.openpkw.openpkwmobile.loaders.PeripheryDataLoader;
import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.models.ElectionCommitteeDTO;
import pl.openpkw.openpkwmobile.models.PeripheryDTO;
import pl.openpkw.openpkwmobile.qr.QrValidator;
import pl.openpkw.openpkwmobile.qr.QrWrapper;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;
import static pl.openpkw.openpkwmobile.utils.Utils.PERMISSION_REQUEST_CAMERA;

public class ScanQrCodeActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    public static HashMap<String,CandidateVoteDTO> candidatesHashMap;
    public static HashSet<String> electionCommitteeDistrictList;
    public static HashMap<String,ElectionCommitteeDTO> electionCommitteeMap;

    private View scanLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        scanLayout = findViewById(R.id.scan_qr_fragment_container);

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

        //check access to camera
        checkPermissions();
    }

    private void showProcessInfo() {
        Snackbar.make(scanLayout, "Krok 3 - Skanowanie kodu QR",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }else
            showProcessInfo();
    }

    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.

            Snackbar snackbar = Snackbar.make(scanLayout,"Zezwól na dostęp do aparatu w celu zeskanowania kodu QR.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK",new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(ScanQrCodeActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            });

            snackbar.show();

            TextView snackBarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackBarTextView.setMaxLines(5);

        } else {
            Snackbar.make(scanLayout,
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
                Snackbar.make(scanLayout, "Aplikacja ma zezwolenie na dostęp do aparatu.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                delayTimer.start();
            } else {
                // Permission request was denied.
                Snackbar.make(scanLayout, "Aplikacja nie ma zezwolenia na dostep do aparatu.",
                        Snackbar.LENGTH_SHORT)
                        .show();
                delayTimer.start();
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case IntentIntegrator.REQUEST_CODE:
            {
                if (resultCode == RESULT_CANCELED){
                    Log.e(Utils.TAG, "SCAN CANCELED");
                }
                else
                {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    String scannedQR = scanResult.getContents();
                    Log.e(Utils.TAG, "SCAN OK");
                    Log.e(Utils.TAG, "QR: "+scannedQR);
                    if(QrValidator.isCorrectQR(scannedQR)) {
                        //decode qr
                        QrWrapper qrWrapper = new QrWrapper(scannedQR);
                        String district_number = "Okręg Wyborczy Nr "+qrWrapper.getDistrictNumber();
                        String territorial_code = qrWrapper.getTerritorialCode();
                        String periphery_number = qrWrapper.getPeripheryNumber();
                        PeripheryDTO periphery = new PeripheryDTO(periphery_number, territorial_code);

                        if (territorial_code != null & periphery_number != null) {
                            PeripheryDataLoader peripheryDataLoader = new PeripheryDataLoader(periphery, ScanQrCodeActivity.this);
                            periphery = peripheryDataLoader.getPeripheryData();

                            //read asynchronous candidates data
                            CandidatesDataLoad candidatesDataLoad = new CandidatesDataLoad();
                            candidatesDataLoad.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, scannedQR);
                        }
                        //write data to shared preferences
                        writeDataToSharedPreferences(scannedQR, periphery.getTerritorialCode(), periphery.getPeripheryNumber(),
                                periphery.getPeripheryName(), periphery.getPeripheryAddress(),district_number);

                        FragmentManager fm = getFragmentManager();
                        ScanQrCodeFragment scanQRFragment = (ScanQrCodeFragment)
                                fm.findFragmentByTag(Utils.SCAN_QR_FRAGMENT_TAG);

                        if (scanQRFragment != null) {
                            scanQRFragment.loadData();
                        }

                        //show info QR scanned
                        Snackbar.make(scanLayout, "Kod QR został zeskanowany. Przejdź dalej.",
                                Snackbar.LENGTH_LONG)
                                .show();
                    }else{
                        showDialogIncorrectQr();
                    }

                }
                break;
            }
        }
    }

    private void showDialogIncorrectQr(){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(ScanQrCodeActivity.this, Utils.DIALOG_STYLE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setMessage(R.string.dialog_incorrect_qr_message)
                .setTitle(R.string.dialog_warning_title)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

    }

    private void writeDataToSharedPreferences(String qr, String territorial_code,
                                              String periphery_number,String periphery_name,
                                              String periphery_address, String district_number) {
        SharedPreferences sharedPref = getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Utils.QR,qr);
        editor.putString(Utils.TERRITORIAL_CODE,territorial_code);
        editor.putString(Utils.PERIPHERY_NUMBER,periphery_number);
        editor.putString(Utils.PERIPHERY_NAME,periphery_name);
        editor.putString(Utils.PERIPHERY_ADDRESS,periphery_address);
        editor.putString(Utils.DISTRICT_NUMBER,district_number);
        editor.apply();
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
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    class CandidatesDataLoad extends AsyncTask<String,Integer,HashMap<String, CandidateVoteDTO>>{

        private HashSet<String> electionCommitteesList;

        @Override
        protected HashMap<String, CandidateVoteDTO> doInBackground(String... strings) {
            QrWrapper qrWrapper = new QrWrapper(strings[0]); //strings[0] -> scanned QR
            HashMap<String,CandidateVoteDTO> candidatesMap;
            String district_number = qrWrapper.getDistrictNumber()+"$";
            Log.e(Utils.TAG,"DISTRICT NUMBER: "+district_number);
            CandidatesDataLoader candidatesDataLoader = new CandidatesDataLoader(district_number,ScanQrCodeActivity.this);
            candidatesMap = candidatesDataLoader.getCandidatesData();
            candidatesMap = qrWrapper.getCandidatesVotes(candidatesMap);
            electionCommitteesList = candidatesDataLoader.electionCommitteeList;
            return candidatesMap ;
        }

        @Override
        protected void onPostExecute(HashMap<String, CandidateVoteDTO> hashMap) {
            candidatesHashMap = hashMap;
            electionCommitteeDistrictList = electionCommitteesList;
            //read asynchronous election committees data
            ElectionCommitteesDataLoad electionCommitteesDataLoad = new ElectionCommitteesDataLoad();
            electionCommitteesDataLoad.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    class ElectionCommitteesDataLoad extends AsyncTask<String,Integer,HashMap<String,ElectionCommitteeDTO>>{

        @Override
        protected HashMap<String, ElectionCommitteeDTO> doInBackground(String... strings) {
            ElectionCommitteesDataLoader electionCommitteesDataLoader = new ElectionCommitteesDataLoader(ScanQrCodeActivity.this);
            return electionCommitteesDataLoader.getElectionCommitteesData();
        }

        @Override
        protected void onPostExecute( HashMap<String,ElectionCommitteeDTO> hashMap) {
            electionCommitteeMap = hashMap;
            if(candidatesHashMap!=null && !candidatesHashMap.isEmpty()
                    && electionCommitteeDistrictList!=null && !electionCommitteeDistrictList.isEmpty()
                    && electionCommitteeMap!=null && !electionCommitteeMap.isEmpty())
            {
                for (String iElectionCommitteeDistrictList : electionCommitteeDistrictList) {
                    ElectionCommitteeDTO electionCommittee = electionCommitteeMap.get(iElectionCommitteeDistrictList.substring(0,iElectionCommitteeDistrictList.indexOf(";")));
                    if(electionCommittee  !=null) {
                        electionCommittee.setListNumber(Integer.valueOf(iElectionCommitteeDistrictList.substring(
                                iElectionCommitteeDistrictList.indexOf(";") + 1, iElectionCommitteeDistrictList.length())));
                        TotalVotesCounter totalVotesCounter = new TotalVotesCounter(candidatesHashMap,electionCommittee.getName());
                        totalVotesCounter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,electionCommittee.getListNumber());
                    }
                }
            }
        }
    }

    class TotalVotesCounter extends AsyncTask<Integer,Integer,Integer>{

        public TotalVotesCounter(HashMap<String, CandidateVoteDTO> candidatesMap, String electionCommitteeName) {
            this.candidatesMap = candidatesMap;
            this.electionCommitteeName = electionCommitteeName;
        }

        private HashMap<String,CandidateVoteDTO> candidatesMap;
        private String electionCommitteeName;

        @Override
        protected Integer doInBackground(Integer... integers) {
            Iterator it = candidatesMap.entrySet().iterator();
            int totalNumberOfVotes = 0;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                CandidateVoteDTO candidate = (CandidateVoteDTO)pair.getValue();
                if(Objects.equals(candidate.getListNumber(), integers[0]))
                {
                    totalNumberOfVotes = totalNumberOfVotes + candidate.getVotesNumber();
                }
            }
            return totalNumberOfVotes;
        }

        @Override
        protected void onPostExecute(Integer totalNumberOfVotes) {
            Log.e(Utils.TAG, "TOTAL TEST NUMBER OF VOTES: " + totalNumberOfVotes);
            ElectionCommitteeDTO electionCommittee = electionCommitteeMap.get(electionCommitteeName);
            if(electionCommittee!=null){
                electionCommittee.setTotalNumberOfVotes(totalNumberOfVotes);
                electionCommitteeMap.put(electionCommitteeName,electionCommittee);
                Log.e(Utils.TAG, "ELECTION COMMITTEE NAME: " + electionCommittee.getName());
                Log.e(Utils.TAG, "TOTAL NUMBER OF VOTES: " + electionCommittee.getTotalNumberOfVotes());
            }
        }
    }

}
