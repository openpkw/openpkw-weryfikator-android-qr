package pl.openpkw.openpkwmobile.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.LoginActivity;
import pl.openpkw.openpkwmobile.models.UserRegisterDTO;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.network.RestClientError;
import pl.openpkw.openpkwmobile.network.SendUserRegisterData;
import pl.openpkw.openpkwmobile.network.UserRegisterResponse;
import pl.openpkw.openpkwmobile.security.KeyWrapper;
import pl.openpkw.openpkwmobile.utils.Utils;


public class RegisterUserFragment extends Fragment {

    private String URL_REGISTER;

    private static final Pattern patternNameSurname = Pattern.compile("^[a-zA-Z]{2,30}");

    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;

    private RelativeLayout userRegisterRelativeLayout;

    boolean isDataCorrect = true;

    private ContextThemeWrapper contextThemeWrapper;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View v = inflater.inflate(R.layout.fragment_register_user, container, false);
        userRegisterRelativeLayout = (RelativeLayout) v.findViewById(R.id.register_relative_layout);
        nameEditText = (EditText) v.findViewById(R.id.register_edittext_name);
        surnameEditText = (EditText) v.findViewById(R.id.register_edittext_surname);
        emailEditText = (EditText) v.findViewById(R.id.register_edittext_email);
        passwordEditText = (EditText) v.findViewById(R.id.register_edittext_password);
        passwordConfirmEditText = (EditText) v.findViewById(R.id.register_edittext_password_confirm);
        passwordConfirmEditText.addTextChangedListener(passwordConfirmTextWatcher);
        Button registerUserButton = (Button) v.findViewById(R.id.register_button_register_user);
        registerUserButton.setOnClickListener(registerUserButtonClickListener);

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    View.OnClickListener registerUserButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            if(emailEditText.getText().toString().isEmpty())
            {
                emailEditText.setError(getString(R.string.register_error_email));
                isDataCorrect = false;
            }
            else {
                if (!Utils.isEmailValid(emailEditText.getText())) {
                    emailEditText.setError(getString(R.string.register_error_email_invalid));
                    isDataCorrect = false;
                }
            }

            Matcher matcher;
            if(nameEditText.getText().toString().isEmpty())
            {
                nameEditText.setError(getString(R.string.register_error_name));
                isDataCorrect = false;
            }
            else {
                matcher = patternNameSurname.matcher(nameEditText.getText().toString().trim());
                isDataCorrect = matcher.matches();
                if(!isDataCorrect )
                    nameEditText.setError(getString(R.string.register_error_name));
            }

            if(surnameEditText.getText().toString().isEmpty())
            {
                surnameEditText.setError(getString(R.string.register_error_surname));
                isDataCorrect = false;
            }
            else {
                matcher = patternNameSurname.matcher(surnameEditText.getText().toString().trim());
                isDataCorrect = matcher.matches();
                if(!isDataCorrect)
                    surnameEditText.setError(getString(R.string.register_error_surname));
            }

            if(passwordEditText.getText().toString().isEmpty())
            {
                passwordEditText.setError(getString(R.string.register_error_password));
                isDataCorrect = false;
            }
            else
            {
                if(passwordEditText.getText().length()<8)
                {
                    passwordEditText.setError(getString(R.string.register_error_password_short));
                    isDataCorrect = false;
                }
                else
                {
                    isDataCorrect = passwordEditText.getText().toString().equals(passwordConfirmEditText.getText().toString());
                }
            }

            if(passwordConfirmEditText.getText().toString().isEmpty())
            {
                passwordConfirmEditText.setError(getString(R.string.register_error_password_confirm));
                isDataCorrect = false;
            }

            if(!passwordConfirmEditText.getText().toString().equals(passwordEditText.getText().toString()))
            {
                isDataCorrect = false;
                passwordConfirmEditText.setError(getString(R.string.register_error_passwords_not_match));
            }

            if(isDataCorrect)
            {
                if(NetworkUtils.isNetworkAvailable(getActivity())) {
                    URL_REGISTER = getUrlRegister();
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
                    KeyWrapper keyWrapper = new KeyWrapper(getActivity().getApplicationContext(), Utils.KEY_ALIAS);
                    UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
                    userRegisterDTO.setPassword(passwordEditText.getText().toString().trim());
                    userRegisterDTO.setEmail(emailEditText.getText().toString().trim());
                    userRegisterDTO.setFirstName(nameEditText.getText().toString().trim());
                    userRegisterDTO.setLastName(surnameEditText.getText().toString().trim());
                    String publicKeyStr = sharedPref.getString(Utils.PUBLIC_KEY, null);
                    PublicKey publicKey = null;
                    try {
                        publicKey = keyWrapper.unwrapPublicKey(Base64.decode(publicKeyStr,Base64.DEFAULT));
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                    }
                    if (publicKey != null) {
                        Log.e(Utils.TAG, "PUBLIC KEY:" + Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                        userRegisterDTO.setPublicKey(Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP));
                    }

                    RegisterUserAsyncTask registerUserAsyncTask = new RegisterUserAsyncTask();
                    registerUserAsyncTask.execute(userRegisterDTO);
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                    builder.setMessage(R.string.login_toast_no_network_connection_message)
                            .setTitle(R.string.login_toast_no_network_connection_title)
                            .setPositiveButton(R.string.zxing_button_ok,null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            else {
                Snackbar.make(userRegisterRelativeLayout, getString(R.string.register_error_data_user),
                        Snackbar.LENGTH_LONG)
                        .show();
            }


        }
    };

    TextWatcher passwordConfirmTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String password = passwordEditText.getText().toString();
            if(editable.length()>password.length())
            {
                passwordConfirmEditText.setError(getString(R.string.register_error_passwords_not_match));
                isDataCorrect = false;
            }
            else {
               if(editable.toString().equals(password.substring(0,editable.length()))){
                   isDataCorrect = true;
               }else{
                   passwordConfirmEditText.setError(getString(R.string.register_error_passwords_not_match));
                   isDataCorrect = false;
               }
            }
        }
    };

    private class RegisterUserAsyncTask extends AsyncTask<UserRegisterDTO, Integer, JSONObject>
    {
        private ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(contextThemeWrapper);
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.register_label_progress_user_register));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
        }

        @Override
        protected JSONObject doInBackground(UserRegisterDTO... userRegisterDTOs) {
            SendUserRegisterData jParser = new SendUserRegisterData();
            Gson gson = new Gson();
            return jParser.sendUserData(URL_REGISTER, gson.toJson(userRegisterDTOs[0]));
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            progressBar.setProgress(100);
            progressBar.dismiss();

            passwordEditText.getText().clear();
            passwordEditText.setError(null);
            passwordConfirmEditText.getText().clear();
            passwordConfirmEditText.setError(null);
            surnameEditText.getText().clear();
            surnameEditText.setError(null);
            nameEditText.getText().clear();
            nameEditText.setError(null);
            emailEditText.getText().clear();
            emailEditText.setError(null);

            if (json != null){
                Log.e(Utils.TAG, "SERVER RESPONSE REGISTER: "+json.toString());
                try {
                    if(!json.getString(Utils.ERROR_MESSAGE).isEmpty()) {

                        Gson gson = new GsonBuilder().create();
                        UserRegisterResponse userRegisterResponse = gson.fromJson(json.toString(), UserRegisterResponse.class);
                        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);

                        switch (userRegisterResponse.getErrorCode()) {
                            case RestClientError.OK:
                                builder.setMessage(R.string.register_toast_user_register_ok_message)
                                        .setTitle(R.string.register_toast_user_register_title)
                                        .setPositiveButton(R.string.session_timeout_login, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(loginIntent);
                                                dialogInterface.dismiss();
                                                getActivity().finish();
                                            }
                                        });
                                break;

                            case RestClientError.USER_ALREADY_EXISTS:

                                builder.setMessage(R.string.register_toast_user_already_exist_message)
                                        .setTitle(R.string.register_toast_user_register_title)
                                        .setPositiveButton(R.string.zxing_button_ok, null);
                                break;

                            default:
                                builder.setMessage(R.string.register_toast_user_register_error_message)
                                        .setTitle(R.string.register_toast_user_register_title)
                                        .setPositiveButton(R.string.zxing_button_ok, null);
                                break;
                        }
                        AlertDialog dialog = builder.create();
                        dialog.show();

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

    private String getUrlRegister()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        return sharedPref.getString(Utils.URL_REGISTER_PREFERENCE, Utils.URL_DEFAULT_REGISTER ).trim();
    }
}
