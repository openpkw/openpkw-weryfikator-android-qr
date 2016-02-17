package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivateKey;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.PasswordRestoreActivity;
import pl.openpkw.openpkwmobile.activities.RegisterUserActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.models.OAuthParam;
import pl.openpkw.openpkwmobile.models.UserCredentialsDTO;
import pl.openpkw.openpkwmobile.network.GetRefreshToken;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.security.SecurityECDSA;
import pl.openpkw.openpkwmobile.utils.StringUtils;


public class LoginFragment extends Fragment {

    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginButton = (Button) v.findViewById(R.id.login_button_login);
        loginButton.setOnClickListener(loginButtonClickListener);

        Button registerUserButton = (Button)v.findViewById(R.id.login_button_register);
        registerUserButton.setOnClickListener(registerUserButtonClickListener);

        Button restorePasswordButton = (Button) v.findViewById(R.id.login_textlink_fpassword);
        SpannableString buttonText = new SpannableString(restorePasswordButton.getText());
        buttonText.setSpan(new UnderlineSpan(), 0, buttonText.length(), 0);
        restorePasswordButton.setText(buttonText);

        emailEditText = (EditText) v.findViewById(R.id.login_edittext_user);
        passwordEditText = (EditText) v.findViewById(R.id.login_edittext_password);

        restorePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent prestoreIntent = new Intent(getActivity(), PasswordRestoreActivity.class);
                startActivity(prestoreIntent);
            }
        });
        return v;
    }

    public View.OnClickListener registerUserButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent registerUserIntent = new Intent(getActivity(), RegisterUserActivity.class);
            startActivity(registerUserIntent);
        }
    };

    public View.OnClickListener loginButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isEmailCorrect;
            if(emailEditText.getText().toString().isEmpty())
            {
                emailEditText.setError(getString(R.string.register_error_email));
                isEmailCorrect = false;
            }
            else {
                if (!StringUtils.isEmailValid(emailEditText.getText().toString().trim())) {
                    emailEditText.setError(getString(R.string.register_error_email_invalid));
                    isEmailCorrect = false;
                }
                else
                    isEmailCorrect = true;
            }

            boolean isPasswordCorrect;
            if(passwordEditText.getText().toString().isEmpty())
            {
                passwordEditText.setError(getString(R.string.register_error_password));
                isPasswordCorrect = false;
            }
            else
                isPasswordCorrect = true;

            if(isPasswordCorrect && isEmailCorrect) {

               if(NetworkUtils.isNetworkAvailable(getActivity())) {
                    //read url login from preference
                    UserCredentialsDTO credentials = new UserCredentialsDTO();
                    credentials.setEmail(emailEditText.getText().toString().trim());
                    credentials.setPassword(passwordEditText.getText().toString().trim());
                    //run login task
                    LoginAsyncTask loginAsyncTask = new LoginAsyncTask(getOAuthLoginParam());
                    loginAsyncTask.execute(credentials);
               }
               else
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.login_toast_no_network_connection),
                            Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.login_toast_enter_login_password),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private class LoginAsyncTask extends AsyncTask<UserCredentialsDTO, String, JSONObject>{

        private OAuthParam oAuthParam;
        private ProgressDialog progressBar;

        private LoginAsyncTask(OAuthParam oAuthParam) {
            this.oAuthParam = oAuthParam;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(getActivity());
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.login_label_progress_authorization));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
        }

        @Override
        protected JSONObject doInBackground(UserCredentialsDTO... credentials) {
            Log.e(StringUtils.TAG, "OAUTH URL: "+oAuthParam.getLoginURL());
            Log.e(StringUtils.TAG, "OAUTH ID: "+oAuthParam.getId());
            Log.e(StringUtils.TAG, "OAUTH SECRET: "+oAuthParam.getSecret());
            GetRefreshToken jParser = new GetRefreshToken();
            return jParser.getToken(oAuthParam.getLoginURL(), oAuthParam.getId(), oAuthParam.getSecret(),
                    credentials[0].getEmail(), credentials[0].getPassword());
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            progressBar.setProgress(100);
            progressBar.dismiss();

            if (json != null){
                Log.e(StringUtils.TAG, "SERVER RESPONSE LOGIN: "+json.toString());
                try {

                    if(!json.getString(StringUtils.ERROR).isEmpty())
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.login_toast_authorization_failed), Toast.LENGTH_LONG).show();

                        emailEditText.getText().clear();
                        emailEditText.setError(null);
                        emailEditText.requestFocus();
                        passwordEditText.getText().clear();
                        passwordEditText.setError(null);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        if(!json.getString(StringUtils.REFRESH_TOKEN).isEmpty()) {
                            String refresh_token = json.getString(StringUtils.REFRESH_TOKEN);

                            if (!refresh_token.isEmpty()) {
                                writeRefreshTokenToSharedPreferences(StringUtils.REFRESH_TOKEN, refresh_token);
                                Intent scanIntent = new Intent(getActivity(), ScanQrCodeActivity.class);
                                startActivity(scanIntent);
                                getActivity().finish();
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

    private void writeRefreshTokenToSharedPreferences(String key, String token) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(StringUtils.DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,token);
        editor.apply();
    }

    private OAuthParam getOAuthLoginParam()
    {
        OAuthParam oAuthParam = new OAuthParam();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(StringUtils.DATA, Context.MODE_PRIVATE);
        String id = sharedPref.getString(StringUtils.OAUTH2_ID_PREFERENCE, null);
        String secret = sharedPref.getString(StringUtils.OAUTH2_SECRET_PREFERENCE, null);
        PrivateKey privateKey = SecurityECDSA.loadPrivateKey(StringUtils.KEY_ALIAS);
        oAuthParam.setLoginURL(sharedPref.getString(StringUtils.URL_LOGIN_PREFERENCE, StringUtils.URL_DEFAULT_LOGIN).trim());

        if(sharedPref.getBoolean(StringUtils.DEFAULT_PARAM_CHANGE,false)){
            String decryptID = SecurityECDSA.decrypt(Base64.decode(id, Base64.DEFAULT), privateKey);
            String decryptSecret =SecurityECDSA.decrypt(Base64.decode(secret,Base64.DEFAULT),privateKey);
            oAuthParam.setId(decryptID);
            oAuthParam.setSecret(decryptSecret);
        } else {
            oAuthParam.setId(sharedPref.getString(StringUtils.OAUTH2_ID_PREFERENCE, StringUtils.ID_DEFAULT).trim());
            oAuthParam.setSecret(sharedPref.getString(StringUtils.OAUTH2_SECRET_PREFERENCE, StringUtils.SECRET_DEFAULT).trim());
        }

        return oAuthParam;
    }

}
