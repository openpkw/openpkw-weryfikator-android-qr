package pl.openpkw.openpkwmobile.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivateKey;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.PasswordRestoreActivity;
import pl.openpkw.openpkwmobile.activities.RegisterUserActivity;
import pl.openpkw.openpkwmobile.models.OAuthParam;
import pl.openpkw.openpkwmobile.models.UserCredentialsDTO;
import pl.openpkw.openpkwmobile.network.GetRefreshToken;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.security.SecurityRSA;
import pl.openpkw.openpkwmobile.utils.TimerSingleton;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity.showToast;


public class LoginFragment extends Fragment implements View.OnClickListener{

    private EditText emailEditText;
    private EditText passwordEditText;
    private ContextThemeWrapper contextThemeWrapper;
    private LoginAsyncTask loginAsyncTask;
    public static TimerSingleton timer;

    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginButton = (Button) v.findViewById(R.id.login_button_login);
        loginButton.setOnClickListener(this);

        Button registerUserButton = (Button)v.findViewById(R.id.login_button_register);
        registerUserButton.setOnClickListener(this);

        Button restorePasswordButton = (Button) v.findViewById(R.id.login_textlink_fpassword);
        SpannableString buttonText = new SpannableString(restorePasswordButton.getText());
        buttonText.setSpan(new UnderlineSpan(), 0, buttonText.length(), 0);
        restorePasswordButton.setText(buttonText);
        restorePasswordButton.setOnClickListener(this);

        emailEditText = (EditText) v.findViewById(R.id.login_edittext_user);
        passwordEditText = (EditText) v.findViewById(R.id.login_edittext_password);

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        timer = new TimerSingleton(Utils.SESSION_TIMER,1000,getActivity().getApplication());

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_button_login:{

                boolean isLoginDataCorrect = true;
                if(emailEditText.getText().toString().isEmpty())
                {
                    emailEditText.setError(getString(R.string.register_error_email));
                    isLoginDataCorrect = false;
                }
                else {
                    if (!Utils.isEmailValid(emailEditText.getText().toString().trim())) {
                        emailEditText.setError(getString(R.string.register_error_email_invalid));
                        isLoginDataCorrect = false;
                    }
                }

                if(passwordEditText.getText().toString().isEmpty())
                {
                    passwordEditText.setError(getString(R.string.register_error_password));
                    isLoginDataCorrect = false;
                }

                if(isLoginDataCorrect) {

                    if(NetworkUtils.isNetworkAvailable(getActivity())) {
                        //read url login from preference
                        UserCredentialsDTO credentials = new UserCredentialsDTO();
                        credentials.setEmail(emailEditText.getText().toString().trim());
                        credentials.setPassword(passwordEditText.getText().toString().trim());
                        //run login task
                        loginAsyncTask = new LoginAsyncTask(getOAuthLoginParam());
                        loginAsyncTask.execute(credentials);
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                        builder.setMessage(R.string.login_toast_no_network_connection_message)
                                .setTitle(R.string.login_toast_no_network_connection_title)
                                .setCancelable(false)
                                .setPositiveButton(R.string.zxing_button_ok, null);
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
                else{
                    showToast(R.string.login_toast_enter_login_password,getActivity(),true);
                }
                break;
            }
            case R.id.login_button_register:{
                Intent registerUserIntent = new Intent(getActivity(), RegisterUserActivity.class);
                startActivity(registerUserIntent);
                break;
            }
            case R.id.login_textlink_fpassword:{
                Intent prestoreIntent = new Intent(getActivity(), PasswordRestoreActivity.class);
                startActivity(prestoreIntent);
                break;
            }
        }
    }

    private class LoginAsyncTask extends AsyncTask<UserCredentialsDTO, String, JSONObject>{

        private OAuthParam oAuthParam;
        private ProgressDialog progressBar;
        private CountDownTimer connectionTimer;

        private LoginAsyncTask(OAuthParam oAuthParam) {
            this.oAuthParam = oAuthParam;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //create progress bar
            progressBar = new ProgressDialog(contextThemeWrapper);
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.login_label_progress_authorization));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.setCancelable(false);
            progressBar.show();
            //set connection timeout 10 sec
            connectionTimer = new CountDownTimer(10000,1000){

                @Override
                public void onTick(long l) {
                    Log.e(Utils.TAG, "CONNECTION TIMEOUT TICK");
                }

                @Override
                public void onFinish() {
                    // stop async task if timeout is reach
                    if (loginAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        loginAsyncTask.cancel(false);

                        if(progressBar!=null)
                            progressBar.dismiss();

                        showToast(R.string.login_toast_no_connection,getActivity(),true);
                    }
                }
            }.start();
        }

        @Override
        protected JSONObject doInBackground(UserCredentialsDTO... credentials) {
            Log.e(Utils.TAG, "OAUTH URL LOGIN: "+oAuthParam.getLoginURL());
            Log.e(Utils.TAG, "OAUTH ID: "+oAuthParam.getId());
            Log.e(Utils.TAG, "OAUTH SECRET: " + oAuthParam.getSecret());
            GetRefreshToken jParser = new GetRefreshToken();
            return jParser.getToken(oAuthParam.getLoginURL(), oAuthParam.getId(), oAuthParam.getSecret(),
                    credentials[0].getEmail(), credentials[0].getPassword());
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            progressBar.setProgress(100);
            progressBar.dismiss();

            connectionTimer.cancel();

            if (json != null){
                Log.e(Utils.TAG, "SERVER RESPONSE LOGIN: "+json.toString());
                try {

                    if(!json.getString(Utils.ERROR).isEmpty())
                    {
                        showToast(R.string.login_toast_authorization_failed,getActivity(),true);

                        emailEditText.getText().clear();
                        emailEditText.setError(null);
                        emailEditText.requestFocus();
                        passwordEditText.getText().clear();
                        passwordEditText.setError(null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        if(!json.getString(Utils.REFRESH_TOKEN).isEmpty()) {
                            String refresh_token = json.getString(Utils.REFRESH_TOKEN);

                            if (!refresh_token.isEmpty()) {
                                String encryptToken = Base64.encodeToString(
                                       SecurityRSA.encrypt(refresh_token, SecurityRSA.loadPublicKey(Utils.KEY_ALIAS)),Base64.DEFAULT);
                                //save encrypted refresh token to shared preferences
                                writeRefreshTokenToSharedPreferences(Utils.REFRESH_TOKEN, encryptToken);
                                //start session timer
                                timer.start();

                                onLoginSuccessfully();
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

            }else{
                showToast(R.string.login_toast_no_connection,getActivity(),true);
            }
        }
    }

    private void writeRefreshTokenToSharedPreferences(String key, String token) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,token);
        editor.apply();
    }

    private OAuthParam getOAuthLoginParam()
    {
        OAuthParam oAuthParam = new OAuthParam();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String id = sharedPref.getString(Utils.OAUTH2_ID_PREFERENCE, null);
        String secret = sharedPref.getString(Utils.OAUTH2_SECRET_PREFERENCE, null);
        PrivateKey privateKey = SecurityRSA.loadPrivateKey(Utils.KEY_ALIAS);
        oAuthParam.setLoginURL(getLoginUrl());

        if(sharedPref.getBoolean(Utils.DEFAULT_PARAM_CHANGE,false)){
            String decryptID = SecurityRSA.decrypt(Base64.decode(id, Base64.DEFAULT), privateKey);
            String decryptSecret =SecurityRSA.decrypt(Base64.decode(secret,Base64.DEFAULT),privateKey);
            oAuthParam.setId(decryptID);
            oAuthParam.setSecret(decryptSecret);
        } else {
            oAuthParam.setId(sharedPref.getString(Utils.OAUTH2_ID_PREFERENCE, Utils.ID_DEFAULT).trim());
            oAuthParam.setSecret(sharedPref.getString(Utils.OAUTH2_SECRET_PREFERENCE, Utils.SECRET_DEFAULT).trim());
        }

        return oAuthParam;
    }

    private String getLoginUrl(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        return sharedPref.getString(Utils.URL_LOGIN_PREFERENCE, Utils.URL_DEFAULT_LOGIN).trim();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
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

    public void onLoginSuccessfully() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }
}
