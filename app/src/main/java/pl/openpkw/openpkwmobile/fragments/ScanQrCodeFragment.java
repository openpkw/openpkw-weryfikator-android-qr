package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.VotingFormActivity;
import pl.openpkw.openpkwmobile.models.OAuthParam;
import pl.openpkw.openpkwmobile.models.QrDTO;
import pl.openpkw.openpkwmobile.network.GetAccessToken;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.network.QrSendResponse;
import pl.openpkw.openpkwmobile.network.SendQrData;
import pl.openpkw.openpkwmobile.security.SecretKeyWrapper;
import pl.openpkw.openpkwmobile.security.SecurityECDSA;
import pl.openpkw.openpkwmobile.utils.StringUtils;

public class ScanQrCodeFragment extends Fragment {

    private String qrString;
    private OAuthParam oAuthParam;

    private IntentIntegrator integratorScan;

    public static TextView textViewQR;
    private TextView textViewSendQR;

    private QrDTO scanQrDTO = null;

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

        Button sendQrButton = (Button) viewScanQR.findViewById(R.id.scan_qr_button_send);
        sendQrButton.setOnClickListener(sendQrButtonClickListener);

        Button forwardButton = (Button) viewScanQR.findViewById(R.id.scan_qr_button_forward);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        Button helpScanQrButton = (Button) viewScanQR.findViewById(R.id.scan_qr_textlink_scan);
        SpannableString buttonText = new SpannableString(helpScanQrButton.getText());
        buttonText.setSpan(new UnderlineSpan(), 0, buttonText.length(), 0);
        helpScanQrButton.setText(buttonText);

        textViewQR = (TextView) viewScanQR.findViewById(R.id.scan_qr_label_top);
        textViewSendQR = (TextView) viewScanQR.findViewById(R.id.scan_qr_label_send_top);

        integratorScan = new IntentIntegrator(getActivity());

        // add spongy castle security provider
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        return viewScanQR;
    }

    public View.OnClickListener sendQrButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            oAuthParam = getOAuthParam();

            if(qrString!=null)
            {
                if(NetworkUtils.isNetworkAvailable(getActivity())) {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(StringUtils.DATA, Context.MODE_PRIVATE);
                    SecretKeyWrapper secretKeyWrapper = new SecretKeyWrapper(getActivity().getApplicationContext(),StringUtils.KEY_ALIAS);
                    String privateKeyStr = sharedPref.getString(StringUtils.PRIVATE_KEY,null);
                    PrivateKey privateKey = null;
                    try {
                        privateKey = secretKeyWrapper.unwrapPrivateKey(Base64.decode(privateKeyStr,Base64.DEFAULT));
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    scanQrDTO = new QrDTO();
                    scanQrDTO.setQr(qrString);
                    scanQrDTO.setToken(oAuthParam.getRefreshToken());
                    scanQrDTO.setSign(Base64.encodeToString(SecurityECDSA.generateSignature(qrString,privateKey),Base64.DEFAULT));

                    GetAccessTokenAsyncTask getAccessTokenAsyncTask = new GetAccessTokenAsyncTask();
                    getAccessTokenAsyncTask.execute(oAuthParam);

                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.scan_qr_toast_send_scanned_qr) +
                                    " " + qrString,
                            Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.login_toast_no_network_connection),
                            Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.scan_qr_toast_please_scan_qr),
                        Toast.LENGTH_LONG).show();
        }
    };

    public View.OnClickListener scanQrButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            textViewSendQR.setText(getString(R.string.scan_qr_label_send));
            integratorScan.initiateScan(IntentIntegrator.QR_CODE_TYPES);
        }
    };

    public View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent vfIntent = new Intent(getActivity(), VotingFormActivity.class);
            startActivity(vfIntent);
            getActivity().finish();
        }
    };

    private class GetAccessTokenAsyncTask extends AsyncTask<OAuthParam, String, JSONObject>{

        @Override
        protected JSONObject doInBackground(OAuthParam... oAuthParams) {
            GetAccessToken jParser = new GetAccessToken();
            return jParser.getToken(oAuthParams[0].getLoginURL(), oAuthParams[0].getId(),
                    oAuthParams[0].getSecret(), oAuthParams[0].getRefreshToken());
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            if (json != null){
                Log.e(StringUtils.TAG, "SERVER RESPONSE ACCESS TOKEN: "+json.toString());
                try {

                    if(!json.getString(StringUtils.ERROR).isEmpty())
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "INTERNAL SERVER ERROR", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        if(!json.getString(StringUtils.ACCESS_TOKEN).isEmpty()) {
                            String access_token = json.getString(StringUtils.ACCESS_TOKEN);

                            if (!access_token.isEmpty()) {
                                if(scanQrDTO!=null) {
                                    Gson gson = new Gson();
                                    SendQrAsyncTask sendQrAsyncTask = new SendQrAsyncTask();
                                    sendQrAsyncTask.execute(access_token, gson.toJson(scanQrDTO));
                                }
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

            }else{
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.login_toast_no_connection), Toast.LENGTH_LONG).show();
            }
        }
    }


    private class SendQrAsyncTask extends AsyncTask<String, String, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... strings) {
            SendQrData jParser = new SendQrData();
            return jParser.sendQR(oAuthParam.getSendQrURL(), strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                Log.e(StringUtils.TAG,"JSON SERVER RESPONSE QR: "+json.toString());
                try {
                    if(!json.getString(StringUtils.ERROR_MESSAGE).isEmpty()) {
                        Gson gson = new GsonBuilder().create();
                        QrSendResponse qrSendResponse = gson.fromJson(json.toString(),QrSendResponse.class);
                        Log.e(StringUtils.TAG,"ERROR MESSAGE: "+ qrSendResponse.getErrorMessage());
                        Log.e(StringUtils.TAG,"PROTOCOL: "+ qrSendResponse.getProtocol());
                        Toast.makeText(getActivity().getApplicationContext(),
                                StringUtils.SERVER_RESPONSE + json.toString(), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.login_toast_no_connection), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void clearQRSharedPreferences(String keyQR, SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyQR, null);
        editor.apply();
    }

    private OAuthParam getOAuthParam()
    {
        OAuthParam oAuthParam = new OAuthParam();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(StringUtils.DATA, Context.MODE_PRIVATE);
        oAuthParam.setLoginURL(sharedPref.getString(StringUtils.URL_LOGIN_PREFERENCE, StringUtils.URL_DEFAULT_LOGIN).trim());
        oAuthParam.setSendQrURL(sharedPref.getString(StringUtils.URL_VERIFY_PREFERENCE, StringUtils.URL_DEFAULT__VERIFY_QR).trim());
        oAuthParam.setRefreshToken(sharedPref.getString(StringUtils.REFRESH_TOKEN, null));
        qrString = sharedPref.getString(StringUtils.QR, null);
        clearQRSharedPreferences(StringUtils.QR, sharedPref);
        if(sharedPref.getBoolean(StringUtils.DEFAULT_PARAM_CHANGE,false)){
            PrivateKey privateKey = SecurityECDSA.loadPrivateKey(StringUtils.KEY_ALIAS);
            String id = sharedPref.getString(StringUtils.OAUTH2_ID_PREFERENCE, StringUtils.ID_DEFAULT).trim();
            String decryptID = SecurityECDSA.decrypt(Base64.decode(id, Base64.DEFAULT), privateKey);
            oAuthParam.setId(decryptID);
            String secret = sharedPref.getString(StringUtils.OAUTH2_SECRET_PREFERENCE, StringUtils.SECRET_DEFAULT).trim();
            String decryptSecret = SecurityECDSA.decrypt(Base64.decode(secret, Base64.DEFAULT), privateKey);
            oAuthParam.setId(decryptSecret);
            Log.e(StringUtils.TAG, "ID: " + decryptID);
            Log.e(StringUtils.TAG, "SECRET: " + decryptSecret);
        }
        else {
            oAuthParam.setId(sharedPref.getString(StringUtils.OAUTH2_ID_PREFERENCE, StringUtils.ID_DEFAULT).trim());
            oAuthParam.setSecret(sharedPref.getString(StringUtils.OAUTH2_SECRET_PREFERENCE, StringUtils.SECRET_DEFAULT).trim());
        }
        return oAuthParam;
    }

}
