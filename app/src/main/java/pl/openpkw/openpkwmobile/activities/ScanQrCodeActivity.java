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
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;
import static pl.openpkw.openpkwmobile.fragments.ScanQrCodeFragment.mCamera;
import static pl.openpkw.openpkwmobile.utils.Utils.CAMERA_ID;
import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.DIALOG_STYLE;
import static pl.openpkw.openpkwmobile.utils.Utils.DISTRICT_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_ADDRESS;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.QR;
import static pl.openpkw.openpkwmobile.utils.Utils.REQUEST_ID_MULTIPLE_PERMISSIONS;
import static pl.openpkw.openpkwmobile.utils.Utils.SCAN_QR_FRAGMENT_TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;
import static pl.openpkw.openpkwmobile.utils.Utils.TIMEOUT_SCAN_QR;

public class ScanQrCodeActivity extends AppCompatActivity implements ScanQrCodeFragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;
    public static HashMap<String,CandidateVoteDTO> candidatesHashMap;
    public static HashSet<String> electionCommitteeDistrictList;
    public static HashMap<String,ElectionCommitteeDTO> electionCommitteeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        FragmentManager fm = getFragmentManager();
        ScanQrCodeFragment scanQRFragment = (ScanQrCodeFragment)
                fm.findFragmentByTag(SCAN_QR_FRAGMENT_TAG );
        if (scanQRFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.scan_qr_fragment_container, new ScanQrCodeFragment(),
                    SCAN_QR_FRAGMENT_TAG );
            ft.commit();
            fm.executePendingTransactions();
        }

        //set title and subtitle to action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("Krok 3 z 9");
            actionBar.setSubtitle("Skanowanie kodu QR");
        }

        //check permissions
        checkAndRequestPermissions();
    }

    private  void checkAndRequestPermissions() {
        int permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int permissionsWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionsWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            android.support.v13.app.ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog saying its necessary and try again otherwise finish app
                        if (android.support.v13.app.ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || android.support.v13.app.ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Dostęp do aparatu fotograficznego i pamięci urządzenia jest wymagany przez tą aplikację do prawidłowego działania. ",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Anuluj", okListener)
                .create()
                .show();
    }

    private Pair<Camera.CameraInfo, Integer> getBackCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return new Pair<Camera.CameraInfo, Integer>(cameraInfo,i);
            }
        }
        return null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case IntentIntegrator.REQUEST_CODE:
            {
                Log.e(TAG, "REQUEST CODE: "+requestCode);
                //release camera
                boolean cameraAccessible = false;
                while(!cameraAccessible){
                    try{
                        mCamera = Camera.open(getBackCamera().second);
                        mCamera.reconnect();
                        cameraAccessible = true;
                    }catch (Exception e){
                        Log.e(TAG,"CAMERA NOT READY: "+e.getMessage());
                    }
                }
                mCamera.release();

                if (resultCode == RESULT_CANCELED){
                    Log.e(TAG, "SCAN CANCELED");
                    showDialogRetryScan();
                }
                else
                {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    String scannedQR = scanResult.getContents();
                    Log.e(TAG, "SCAN OK");
                    Log.e(TAG, "QR: "+scannedQR);
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
                                fm.findFragmentByTag(SCAN_QR_FRAGMENT_TAG);

                        if (scanQRFragment != null) {
                            scanQRFragment.loadData();
                        }

                        //show info QR scanned
                        Toast.makeText(this, "Kod QR został zeskanowany. Przejdź dalej.", Toast.LENGTH_SHORT).show();

                    }else{
                        showDialogIncorrectQr();
                    }

                }
                break;
            }
        }
    }

    private void showDialogRetryScan() {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(ScanQrCodeActivity.this, DIALOG_STYLE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setMessage("Skanowanie kodu QR zakończyło się niepowodzeniem. ")
                .setTitle(R.string.dialog_warning_title)
                .setCancelable(false)
                .setNeutralButton("Instrukcja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent instructionScanQrIntent = new Intent(ScanQrCodeActivity.this, InstructionScanQrActivity.class);
                        startActivity(instructionScanQrIntent);
                        finish();
                    }
                })
                .setNegativeButton("Zakończ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setPositiveButton("Ponów", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        IntentIntegrator integrator = new IntentIntegrator(ScanQrCodeActivity.this);
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                        integrator.setPrompt("Skanuj kod QR z ostatniej strony protokołu wyborczego");
                        integrator.setCameraId(CAMERA_ID);  // Use a specific camera of the device
                        integrator.setBeepEnabled(true);
                        integrator.setBarcodeImageEnabled(true);
                        integrator.setOrientationLocked(true);
                        integrator.setTimeout(TIMEOUT_SCAN_QR);
                        integrator.initiateScan();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogIncorrectQr(){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(ScanQrCodeActivity.this, DIALOG_STYLE);
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
        SharedPreferences sharedPref = getSharedPreferences(DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(QR,qr);
        editor.putString(TERRITORIAL_CODE,territorial_code);
        editor.putString(PERIPHERY_NUMBER,periphery_number);
        editor.putString(PERIPHERY_NAME,periphery_name);
        editor.putString(PERIPHERY_ADDRESS,periphery_address);
        editor.putString(DISTRICT_NUMBER,district_number);
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.e(TAG,"BUTTON SCAN PRESSED");
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    class CandidatesDataLoad extends AsyncTask<String,Integer,HashMap<String, CandidateVoteDTO>>{

        private HashSet<String> electionCommitteesList;

        @Override
        protected HashMap<String, CandidateVoteDTO> doInBackground(String... strings) {
            QrWrapper qrWrapper = new QrWrapper(strings[0]); //strings[0] -> scanned QR
            HashMap<String,CandidateVoteDTO> candidatesMap;
            String district_number = qrWrapper.getDistrictNumber()+"$";
            Log.e(TAG,"DISTRICT NUMBER: "+district_number);
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
            Log.e(TAG, "TOTAL TEST NUMBER OF VOTES: " + totalNumberOfVotes);
            ElectionCommitteeDTO electionCommittee = electionCommitteeMap.get(electionCommitteeName);
            if(electionCommittee!=null){
                electionCommittee.setTotalNumberOfVotes(totalNumberOfVotes);
                electionCommitteeMap.put(electionCommitteeName,electionCommittee);
                Log.e(TAG, "ELECTION COMMITTEE NAME: " + electionCommittee.getName());
                Log.e(TAG, "TOTAL NUMBER OF VOTES: " + electionCommittee.getTotalNumberOfVotes());
            }
        }
    }

}
