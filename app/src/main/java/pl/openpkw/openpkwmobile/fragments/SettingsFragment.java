package pl.openpkw.openpkwmobile.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.security.SecurityRSA;
import pl.openpkw.openpkwmobile.utils.StringUtils;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference urlLoginPreference;
    private EditTextPreference urlRegisterPreference;
    private EditTextPreference urlVerifyPreference;
    private EditTextPreference urlElectionResultPreference;
    private Preference setIdAndSecretPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PREFERENCE STYLE
        Context contextSettings = getActivity();
        contextSettings.setTheme(R.style.MyPreferenceTheme);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //read preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //set summary preference
        urlLoginPreference = (EditTextPreference) findPreference(StringUtils.URL_LOGIN_PREFERENCE);
        urlLoginPreference.setSummary(sharedPref.getString(StringUtils.URL_LOGIN_PREFERENCE,""));
        urlRegisterPreference = (EditTextPreference) findPreference(StringUtils.URL_REGISTER_PREFERENCE);
        urlRegisterPreference.setSummary(sharedPref.getString(StringUtils.URL_REGISTER_PREFERENCE,""));
        urlVerifyPreference  = (EditTextPreference) findPreference(StringUtils.URL_VERIFY_PREFERENCE );
        urlVerifyPreference.setSummary(sharedPref.getString(StringUtils.URL_VERIFY_PREFERENCE,""));
        urlElectionResultPreference  = (EditTextPreference) findPreference(StringUtils.URL_ELECTION_RESULT_PREFERENCE);
        urlElectionResultPreference.setSummary(sharedPref.getString(StringUtils.URL_ELECTION_RESULT_PREFERENCE,""));
        //
        setIdAndSecretPreference = findPreference(StringUtils.OAUTH2_PREFERENCE);
        setIdAndSecretPreference.setOnPreferenceClickListener(setIdAndSecretPreferenceClickListener);

    }

    public Preference.OnPreferenceClickListener setIdAndSecretPreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            final View dialogView = layoutInflater.inflate(R.layout.url_dialog_preference, (ViewGroup) getActivity().findViewById(R.id.setCryptographicKey));
            final EditText idEditText = (EditText) dialogView.findViewById(R.id.dialog_edittext_id);
            idEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(idEditText.getApplicationWindowToken(), 0);
            final EditText secretEditText = (EditText) dialogView.findViewById(R.id.dialog_edittext_password);
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.PreferenceDialogStyle));
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton("OK", null);
            dialogBuilder.setNegativeButton("Anuluj", null);
            final AlertDialog setIdSecretDialog;
            setIdSecretDialog = dialogBuilder.create();
            setIdSecretDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            setIdSecretDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            setIdSecretDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final Button alertDialogButtonPositive =  setIdSecretDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    alertDialogButtonPositive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            boolean isCorrect = true;

                            if(idEditText.getText().toString().isEmpty())
                            {
                                idEditText.setError("Proszę uzupełnić pole identyfikator");
                                isCorrect = false;
                            }

                            if(secretEditText.getText().toString().isEmpty())
                            {
                                secretEditText.setError("Proszę uzupełnić pole hasło");
                                isCorrect = false;
                            }

                            if(isCorrect)
                            {
                                String id = (idEditText.getText().toString());
                                String secret = (secretEditText.getText().toString());
                                Log.e(StringUtils.TAG,"NEW ID: "+id);
                                Log.e(StringUtils.TAG,"NEW secret: "+secret);
                                SharedPreferences sharedPref = getActivity().getSharedPreferences(StringUtils.DATA, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(StringUtils.OAUTH2_ID_PREFERENCE,
                                        Base64.encodeToString(SecurityRSA.encrypt(id,SecurityRSA.loadPublicKey(StringUtils.KEY_ALIAS)),Base64.DEFAULT));
                                editor.putString(StringUtils.OAUTH2_SECRET_PREFERENCE,
                                        Base64.encodeToString(SecurityRSA.encrypt(secret, SecurityRSA.loadPublicKey(StringUtils.KEY_ALIAS)),Base64.DEFAULT));
                                editor.putBoolean(StringUtils.DEFAULT_PARAM_CHANGE, true);
                                editor.apply();
                                setIdSecretDialog.dismiss();
                            }

                        }
                    });

                }
            });
            setIdSecretDialog.show();

            return false;
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        if(rootView!=null) {
            ListView list = (ListView) rootView.findViewById(android.R.id.list);
            list.setDivider(new ColorDrawable(getResources().getColor(R.color.green)));
            list.setDividerHeight(4);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(StringUtils.URL_LOGIN_PREFERENCE)) {
            Log.e(StringUtils.TAG,"LOGIN URL "+sharedPreferences.getString(key,""));
            urlLoginPreference.setSummary(sharedPreferences.getString(key,""));
        }
        if (key.equals(StringUtils.URL_REGISTER_PREFERENCE)) {
            Log.e(StringUtils.TAG,"REGISTER URL: "+sharedPreferences.getString(key,""));
            urlRegisterPreference.setSummary(sharedPreferences.getString(key,""));
        }
        if (key.equals(StringUtils.URL_VERIFY_PREFERENCE)) {
            Log.e(StringUtils.TAG,"Vetify URL: "+sharedPreferences.getString(key,""));
            urlVerifyPreference.setSummary(sharedPreferences.getString(key,""));
        }
        if (key.equals(StringUtils.URL_ELECTION_RESULT_PREFERENCE)) {
            Log.e(StringUtils.TAG,"Election result URL: "+sharedPreferences.getString(key,""));
            urlElectionResultPreference.setSummary(sharedPreferences.getString(key,""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

