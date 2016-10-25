package pl.openpkw.openpkwmobile.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.ScanQrCodeFragment;
import pl.openpkw.openpkwmobile.loaders.CandidatesDataLoader;
import pl.openpkw.openpkwmobile.loaders.ElectionCommitteesDataLoader;
import pl.openpkw.openpkwmobile.loaders.PeripheryDataLoader;
import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.models.ElectionCommitteeDTO;
import pl.openpkw.openpkwmobile.models.PeripheryDTO;
import pl.openpkw.openpkwmobile.qr.QrValidator;
import pl.openpkw.openpkwmobile.qr.QrWrapper;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;
import static pl.openpkw.openpkwmobile.utils.Utils.CLASS_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.DIALOG_STYLE;
import static pl.openpkw.openpkwmobile.utils.Utils.DISTRICT_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_ADDRESS;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.QR;
import static pl.openpkw.openpkwmobile.utils.Utils.SCAN_QR_FRAGMENT_TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;

public class QrCodeCaptureActivity extends AppCompatActivity implements View.OnClickListener{
    private DecoratedBarcodeView barcodeView;
    public static HashMap<String,CandidateVoteDTO> candidatesHashMap;
    public static HashSet<String> electionCommitteeDistrictList;
    public static HashMap<String,ElectionCommitteeDTO> electionCommitteeMap;
    private boolean doubleBackToExitPressedOnce = false;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String scannedQR = result.getText();
                if(QrValidator.isCorrectQR(scannedQR)) {
                    //decode qr
                    QrWrapper qrWrapper = new QrWrapper(scannedQR);
                    String district_number = "Okręg Wyborczy Nr "+qrWrapper.getDistrictNumber();
                    String territorial_code = qrWrapper.getTerritorialCode();
                    String periphery_number = qrWrapper.getPeripheryNumber();
                    PeripheryDTO periphery = new PeripheryDTO(periphery_number, territorial_code);

                    if (territorial_code != null & periphery_number != null) {
                        PeripheryDataLoader peripheryDataLoader = new PeripheryDataLoader(periphery, QrCodeCaptureActivity.this);
                        periphery = peripheryDataLoader.getPeripheryData();

                        //read asynchronous candidates data
                        CandidatesDataLoad candidatesDataLoad = new CandidatesDataLoad();
                        candidatesDataLoad.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, scannedQR);
                    }
                    //write data to shared preferences
                    writeDataToSharedPreferences(scannedQR, periphery.getTerritorialCode(), periphery.getPeripheryNumber(),
                            periphery.getPeripheryName(), periphery.getPeripheryAddress(),district_number);

                    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                    ScanQrCodeFragment scanQRFragment = (ScanQrCodeFragment)fm.findFragmentByTag(SCAN_QR_FRAGMENT_TAG);

                    if (scanQRFragment != null) {
                        scanQRFragment.loadData();
                    }

                    //show info QR scanned OK
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.toast_scanned_qr_ok, Toast.LENGTH_LONG);
                    View view = toast.getView();
                    view.setBackgroundResource(R.drawable.toast_green);
                    TextView text = (TextView) view.findViewById(android.R.id.message);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                        text.setTextColor(getResources().getColor(android.R.color.white,getTheme()));
                    else
                        text.setTextColor(getResources().getColor(android.R.color.white));
                    text.setTypeface(Typeface.DEFAULT_BOLD);
                    text.setGravity(Gravity.CENTER);
                    toast.show();

                    Intent intent = new Intent(getApplicationContext(),ScanQrCodeActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    showDialogIncorrectQr();
                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

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

    private void showDialogIncorrectQr(){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(QrCodeCaptureActivity.this, DIALOG_STYLE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setMessage(R.string.dialog_incorrect_qr_message)
                .setTitle(R.string.dialog_warning_title)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        barcodeView.decodeSingle(callback);
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture_qrcode);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.capture_qrcode_barcode_scanner);
        barcodeView.decodeSingle(callback);
        barcodeView.setStatusText("Zeskanuj kod QR z ostatniej strony protokołu wyborczego");

        Button backButton = (Button) findViewById(R.id.capture_qrcode_back_button);
        backButton.setOnClickListener(this);

        Button instructionButton = (Button) findViewById(R.id.capture_qrcode_instruction_button);
        instructionButton.setOnClickListener(this);

        //set title and subtitle to action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("Krok 3 z 9");
            actionBar.setSubtitle("Skanowanie kodu QR");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.capture_qrcode_back_button:{
                Intent intent = new Intent(this, ScanQrCodeActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.capture_qrcode_instruction_button:{
                Intent intent = new Intent(this, InstructionScanQrActivity.class);
                intent.putExtra(CLASS_NAME,this.getClass().getSimpleName());
                startActivity(intent);
                finish();
                break;
            }
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

    class CandidatesDataLoad extends AsyncTask<String,Integer,HashMap<String, CandidateVoteDTO>>{

        private HashSet<String> electionCommitteesList;

        @Override
        protected HashMap<String, CandidateVoteDTO> doInBackground(String... strings) {
            QrWrapper qrWrapper = new QrWrapper(strings[0]); //strings[0] -> scanned QR
            HashMap<String,CandidateVoteDTO> candidatesMap;
            String district_number = qrWrapper.getDistrictNumber()+"$";
            Log.e(TAG,"DISTRICT NUMBER: "+district_number);
            CandidatesDataLoader candidatesDataLoader = new CandidatesDataLoader(district_number,QrCodeCaptureActivity.this);
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
            ElectionCommitteesDataLoader electionCommitteesDataLoader = new ElectionCommitteesDataLoader(QrCodeCaptureActivity.this);
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

