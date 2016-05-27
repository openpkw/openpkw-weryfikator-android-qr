package pl.openpkw.openpkwmobile.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

import java.security.Security;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.VotingFormActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

public class ScanQrCodeFragment extends Fragment {


    private IntentIntegrator integratorScan;
    private ContextThemeWrapper contextThemeWrapper;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;
    private TextView peripheryNameTextView;
    private TextView peripheryAddressTextView;

    private Button nextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewScanQR  = inflater.inflate(R.layout.fragment_scan_qrcode, container, false);

        Button scanQrButton = (Button) viewScanQR.findViewById(R.id.scan_qr_button_scan);
        scanQrButton.setOnClickListener(scanQrButtonClickListener);

        nextButton = (Button)viewScanQR.findViewById(R.id.scan_qr_next_button);
        nextButton.setOnClickListener(nextButtonClickListener);
        nextButton.setEnabled(true);

        Button helpScanQrButton = (Button) viewScanQR.findViewById(R.id.scan_qr_textlink_scan);
        SpannableString buttonText = new SpannableString(helpScanQrButton.getText());
        buttonText.setSpan(new UnderlineSpan(), 0, buttonText.length(), 0);
        helpScanQrButton.setText(buttonText);

        territorialCodeTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_territorial_code);
        peripheryNumberTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_periphery_number);
        peripheryNameTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_periphery_name);
        peripheryAddressTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_periphery_address);

        integratorScan = new IntentIntegrator(getActivity());

        // add spongy castle security provider
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        loadData();

        return viewScanQR;
    }

    public View.OnClickListener scanQrButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int permissionCamera = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);
            Log.e(Utils.TAG, "PERMISSION CAMERA: "+permissionCamera);

            int permissionFlashlight = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.FLASHLIGHT);
            Log.e(Utils.TAG, "PERMISSION FLASHLIGHT: "+permissionFlashlight);

            if(permissionCamera == PackageManager.PERMISSION_GRANTED ) {
                integratorScan.initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                builder.setMessage("Aplikacja nie ma uprawnień do obsługi aparatu telefonu. Proszę zezwolić aplikacji na korzystanie z apratu.")
                        .setTitle(R.string.dialog_warning_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.zxing_button_ok, null);
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    };

    public View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //if(isQrScanned()) {
            if(true) {
                Intent vfIntent = new Intent(getActivity(), VotingFormActivity.class);
                startActivity(vfIntent);
                getActivity().finish();
            }else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                builder.setMessage("Aby przejść dalej proszę zeskanować kod QR z ostaniej strony protokołu wyborczego")
                        .setTitle(R.string.dialog_warning_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.zxing_button_ok, null);
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    };

    private boolean isQrScanned(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        return sharedPref.getString(Utils.QR, null) != null;
    }

    public void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String territorial_code = sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny");
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        String periphery_name = sharedPref.getString(Utils.PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(Utils.PERIPHERY_ADDRESS, "Adres");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0 ,territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText( spannable);
        peripheryNumberTextView.setText(periphery_number);
        peripheryNameTextView.setText(periphery_name);
        peripheryAddressTextView.setText(periphery_address);
        nextButton.setEnabled(true);
    }

}
