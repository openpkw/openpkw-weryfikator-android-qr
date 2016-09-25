package pl.openpkw.openpkwmobile.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.EndActivity;
import pl.openpkw.openpkwmobile.activities.QueryAddPhotosActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.models.OAuthParam;
import pl.openpkw.openpkwmobile.models.QrDTO;
import pl.openpkw.openpkwmobile.network.GetAccessToken;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.network.QrSendResponse;
import pl.openpkw.openpkwmobile.network.SendQrData;
import pl.openpkw.openpkwmobile.security.KeyWrapper;
import pl.openpkw.openpkwmobile.security.SecurityECC;
import pl.openpkw.openpkwmobile.security.SecurityRSA;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SendDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SendDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendDataFragment extends Fragment {

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;
    private TextView peripheryNameTextView;
    private TextView peripheryAddressTextView;

    private ContextThemeWrapper contextThemeWrapper;

    private String qrString;
    private OAuthParam oAuthParam;
    private QrDTO scanQrDTO = null;

    private OnFragmentInteractionListener mListener;

    public SendDataFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SendDataFragment newInstance() {
       return new SendDataFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View sendDataView = inflater.inflate(R.layout.fragment_send_data, container, false);

        Button sendButton = (Button) sendDataView.findViewById(R.id.send_data_button_send);
        sendButton.setOnClickListener(sendButtonClickListener);

        Button forwardButton = (Button) sendDataView.findViewById(R.id.send_data_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        territorialCodeTextView = (TextView) sendDataView.findViewById(R.id.send_data_territorial_code);
        peripheryNumberTextView = (TextView)sendDataView.findViewById(R.id.send_data_periphery_number);
        peripheryNameTextView = (TextView)sendDataView.findViewById(R.id.send_data_periphery_name);
        peripheryAddressTextView = (TextView) sendDataView.findViewById(R.id.send_data_periphery_address);

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        loadData();

        return sendDataView;
    }

    public void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String territorial_code = "  "+sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny")+"  ";
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        String periphery_name = sharedPref.getString(Utils.PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(Utils.PERIPHERY_ADDRESS, "Adres");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN),0, territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText( spannable);
        peripheryNumberTextView.setText(periphery_number);
        peripheryNameTextView.setText(periphery_name);
        peripheryAddressTextView.setText(periphery_address);
    }

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent queryAddIntent = new Intent(getActivity(), QueryAddPhotosActivity.class);
            startActivity(queryAddIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener sendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            oAuthParam = getOAuthParam();
            final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);

            if(qrString!=null)
            {
                if(NetworkUtils.isNetworkAvailable(getActivity())) {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
                    KeyWrapper keyWrapper = new KeyWrapper(getActivity().getApplicationContext(), Utils.KEY_ALIAS);
                    String privateKeyStr = sharedPref.getString(Utils.PRIVATE_KEY,null);
                    PrivateKey privateKey = null;
                    try {
                        privateKey = keyWrapper.unwrapPrivateKey(Base64.decode(privateKeyStr,Base64.DEFAULT));
                    } catch (GeneralSecurityException | IOException e) {
                        Log.e(Utils.TAG, "ERROR UNWRAP PRIVATE KEY ECDSA: "+e.getMessage());
                    }

                    scanQrDTO = new QrDTO();
                    scanQrDTO.setQr(qrString);
                    byte [] signature = SecurityECC.generateSignature(qrString, privateKey);
                    if(signature!=null) {
                        scanQrDTO.setToken(Base64.encodeToString(signature, Base64.NO_WRAP));
                    }else
                        Log.e(Utils.TAG, "SIGNATURE FAILED");

                    GetAccessTokenAsyncTask getAccessTokenAsyncTask = new GetAccessTokenAsyncTask();
                    getAccessTokenAsyncTask.execute(oAuthParam);

                }else
                {
                    builder.setMessage(R.string.login_toast_no_network_connection_message)
                            .setTitle(R.string.login_toast_no_network_connection_title)
                            .setCancelable(false)
                            .setPositiveButton(R.string.zxing_button_ok, null);
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            else {
                builder.setMessage("Proszę zeskanować kod QR z ostaniej strony protokołu wyborczego")
                        .setTitle(R.string.dialog_warning_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.zxing_button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //start scan activity
                                Intent scanIntent = new Intent(getActivity(), ScanQrCodeActivity.class);
                                startActivity(scanIntent);
                                getActivity().finish();
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

        }
    };

    private OAuthParam getOAuthParam()
    {
        String [] urls = getUrls();
        OAuthParam oAuthParam = new OAuthParam();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        oAuthParam.setLoginURL(urls[0]);
        oAuthParam.setSendQrURL(urls[1]);
        String decryptToken = SecurityRSA.decrypt(Base64.decode(sharedPref.getString(Utils.REFRESH_TOKEN, null)
                , Base64.DEFAULT), SecurityRSA.loadPrivateKey(Utils.KEY_ALIAS));
        oAuthParam.setRefreshToken(decryptToken);
        qrString = sharedPref.getString(Utils.QR, null);

        if(sharedPref.getBoolean(Utils.DEFAULT_PARAM_CHANGE,false)){
            PrivateKey privateKey = SecurityRSA.loadPrivateKey(Utils.KEY_ALIAS);
            String id = sharedPref.getString(Utils.OAUTH2_ID_PREFERENCE, Utils.ID_DEFAULT).trim();
            String decryptID = SecurityRSA.decrypt(Base64.decode(id, Base64.DEFAULT), privateKey);
            oAuthParam.setId(decryptID);
            String secret = sharedPref.getString(Utils.OAUTH2_SECRET_PREFERENCE, Utils.SECRET_DEFAULT).trim();
            String decryptSecret = SecurityRSA.decrypt(Base64.decode(secret, Base64.DEFAULT), privateKey);
            oAuthParam.setSecret(decryptSecret);
        }
        else {
            oAuthParam.setId(sharedPref.getString(Utils.OAUTH2_ID_PREFERENCE, Utils.ID_DEFAULT).trim());
            oAuthParam.setSecret(sharedPref.getString(Utils.OAUTH2_SECRET_PREFERENCE, Utils.SECRET_DEFAULT).trim());
        }
        return oAuthParam;
    }

    private String [] getUrls(){
        String [] urls = new String[2];
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        urls[0]= sharedPref.getString(Utils.URL_LOGIN_PREFERENCE, Utils.URL_DEFAULT_LOGIN).trim();
        urls[1]= sharedPref.getString(Utils.URL_VERIFY_PREFERENCE, Utils.URL_DEFAULT__VERIFY_QR).trim();
        return urls;
    }

    private void clearQRSharedPreferences(String keyQR, SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyQR, null);
        editor.apply();
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

    private class GetAccessTokenAsyncTask extends AsyncTask<OAuthParam, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(OAuthParam... oAuthParams) {
            GetAccessToken jParser = new GetAccessToken();
            return jParser.getToken(oAuthParams[0].getLoginURL(), oAuthParams[0].getId(),
                    oAuthParams[0].getSecret(), oAuthParams[0].getRefreshToken());
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            if (json != null){
                Log.e(Utils.TAG, "SERVER RESPONSE ACCESS TOKEN: "+json.toString());
                try {

                    if(!json.getString(Utils.ERROR).isEmpty())
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Błąd dostępu do serwera", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        if(!json.getString(Utils.ACCESS_TOKEN).isEmpty()) {
                            String access_token = json.getString(Utils.ACCESS_TOKEN);

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

        private ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //create progress bar
            progressBar = new ProgressDialog(contextThemeWrapper);
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.scan_qr_progress_send_data));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.setCancelable(false);
            progressBar.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            SendQrData jParser = new SendQrData();
            return jParser.sendQR(oAuthParam.getSendQrURL(), strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            progressBar.setProgress(100);
            progressBar.dismiss();

            if (json != null) {
                Log.e(Utils.TAG,"JSON SERVER RESPONSE QR: "+json.toString());
                try {
                    if(!json.getString(Utils.ERROR_MESSAGE).isEmpty()) {
                        Gson gson = new GsonBuilder().create();
                        QrSendResponse qrSendResponse = gson.fromJson(json.toString(),QrSendResponse.class);
                        Log.e(Utils.TAG,"ERROR MESSAGE: "+ qrSendResponse.getErrorMessage());
                        Log.e(Utils.TAG,"PROTOCOL: "+ qrSendResponse.getProtocol());

                        Toast.makeText(getActivity().getApplicationContext(),
                                Utils.SERVER_RESPONSE + json.toString(), Toast.LENGTH_LONG).show();

                        //clear QR data
                        clearQRSharedPreferences(Utils.QR, getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE));
                        //cancel timer
                        timer.cancel();
                        //start end activity
                        Intent endIntent = new Intent(getActivity(), EndActivity.class);
                        startActivity(endIntent);
                        getActivity().finish();
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

}
