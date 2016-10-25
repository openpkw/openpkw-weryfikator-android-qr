package pl.openpkw.openpkwmobile.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.UnderlineSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.security.Security;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.InstructionScanQrActivity;
import pl.openpkw.openpkwmobile.activities.QrCodeCaptureActivity;
import pl.openpkw.openpkwmobile.activities.VotingFormActivity;

import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.DIALOG_STYLE;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_ADDRESS;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.PERMISSION_REQUEST_CAMERA;
import static pl.openpkw.openpkwmobile.utils.Utils.QR;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;

public class ScanQrCodeFragment extends Fragment implements View.OnClickListener{

    private ContextThemeWrapper contextThemeWrapper;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;
    private TextView peripheryNameTextView;
    private TextView peripheryAddressTextView;

    public static Camera mCamera;

    private Button nextButton;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewScanQR  = inflater.inflate(R.layout.fragment_scan_qrcode, container, false);

        Button scanQrButton = (Button) viewScanQR.findViewById(R.id.scan_qr_button_scan);
        scanQrButton.setOnClickListener(this);

        nextButton = (Button)viewScanQR.findViewById(R.id.scan_qr_next_button);
        nextButton.setOnClickListener(this);
        nextButton.setEnabled(true);

        Button helpScanQrButton = (Button) viewScanQR.findViewById(R.id.scan_qr_textlink_scan);
        helpScanQrButton.setOnClickListener(this);
        SpannableString buttonText = new SpannableString(helpScanQrButton.getText());
        buttonText.setSpan(new UnderlineSpan(), 0, buttonText.length(), 0);
        helpScanQrButton.setText(buttonText);

        territorialCodeTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_territorial_code);
        peripheryNumberTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_periphery_number);
        peripheryNameTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_periphery_name);
        peripheryAddressTextView = (TextView) viewScanQR.findViewById(R.id.scan_qr_periphery_address);

        // add spongy castle security provider
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), DIALOG_STYLE);

        loadData();

        return viewScanQR;
    }

    private boolean isQrScanned(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
        return sharedPref.getString(QR, null) != null;
    }

    public void loadData() {
        peripheryAddressTextView.setText("Adres: ");
        peripheryAddressTextView.measure(0,0);
        peripheryAddressTextView.setEllipsize(null);
        peripheryAddressTextView.setMaxLines(1);
        int addressLabelTextWidth = peripheryAddressTextView.getMeasuredWidth();
        peripheryNameTextView.setText("Nazwa: ");
        peripheryNameTextView.measure(0,0);
        peripheryNameTextView.setEllipsize(null);
        peripheryNameTextView.setMaxLines(1);
        int peripheryNameLabelTextWidth = peripheryNameTextView.getMeasuredWidth();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
        String territorial_code = sharedPref.getString(TERRITORIAL_CODE, "Kod terytorialny: _ _ _ _");
        if(!territorial_code.equalsIgnoreCase("Kod terytorialny: _ _ _ _"))
            territorial_code = "Kod terytorialny: "+territorial_code;
        String periphery_number = sharedPref.getString(PERIPHERY_NUMBER, "Nr obwodu: _ _ _ _");
        if(!periphery_number.equalsIgnoreCase("Nr obwodu: _ _ _ _"))
            periphery_number = "Nr obwodu: "+periphery_number;
        String periphery_name = sharedPref.getString(PERIPHERY_NAME, "Nazwa: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        if(!periphery_name.equalsIgnoreCase("Nazwa: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _")) {
            periphery_name = "Nazwa: " + periphery_name;
            peripheryNameTextView.setMaxLines(2);
        }
        String periphery_address = sharedPref.getString(PERIPHERY_ADDRESS, "Adres: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        if(!periphery_address.equalsIgnoreCase("Adres: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _")) {
            periphery_address = "Adres: " + periphery_address;
            peripheryAddressTextView.setMaxLines(2);
        }
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN),"Kod terytorialny: ".length(), territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        peripheryNameTextView.setText(createIndentedText(periphery_name,0,peripheryNameLabelTextWidth ));
        peripheryAddressTextView.setText(createIndentedText(periphery_address,0,addressLabelTextWidth));
        nextButton.setEnabled(true);
    }

    public static SpannableString createIndentedText(String text, int marginFirstLine, int marginNextLines) {
        SpannableString result=new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0,text.length(),0);
        return result;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.scan_qr_next_button:{
                if(isQrScanned()) {
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
                break;
            }

            case R.id.scan_qr_textlink_scan:{
                Intent instructionScanQrIntent = new Intent(getActivity(), InstructionScanQrActivity.class);
                startActivity(instructionScanQrIntent);
                getActivity().finish();
                break;
            }

            case R.id.scan_qr_button_scan:{
                int permissionCamera = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.CAMERA);

                if(permissionCamera == PackageManager.PERMISSION_GRANTED ) {

                    Intent captureQrCodeIntent = new Intent(getActivity(), QrCodeCaptureActivity.class);
                    startActivity( captureQrCodeIntent);
                    getActivity().finish();
                    break;

                    /*
                    IntentIntegrator integrator = new IntentIntegrator(getActivity());
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                    integrator.setPrompt("");
                    integrator.setCameraId(CAMERA_ID);  // Use a specific camera of the device
                    integrator.setBeepEnabled(true);
                    integrator.setBarcodeImageEnabled(true);
                    integrator.setOrientationLocked(true);
                    integrator.setTimeout(TIMEOUT_SCAN_QR);
                    integrator.initiateScan();
                    */

                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                    builder.setMessage("Aplikacja nie ma uprawnień do obsługi aparatu telefonu. Proszę zezwolić aplikacji na korzystanie z apratu.")
                            .setTitle(R.string.dialog_warning_title)
                            .setCancelable(false)
                            .setPositiveButton(R.string.zxing_button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Request the camera permission
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.CAMERA},
                                            PERMISSION_REQUEST_CAMERA);
                                }
                            });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
