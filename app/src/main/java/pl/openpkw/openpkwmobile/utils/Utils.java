package pl.openpkw.openpkwmobile.utils;

import android.text.TextUtils;

/**
 * Created by Admin on 08.12.15.
 */
public class Utils {

    public static final String TAG = "OPEN_PKW";
    public static final String URL_DEFAULT__VERIFY_QR = "http://rumcajs.open-pkw.pl:9080/openpkw/api/qr";
    public static final String URL_DEFAULT_LOGIN = "http://rumcajs.open-pkw.pl:9080/openpkw/api/login";
    public static final String URL_DEFAULT_REGISTER = "http://rumcajs.open-pkw.pl:9080/openpkw/users/";
    public static final String URL_DEFAULT_ELECTION_RESULT = "http://rumcajs.open-pkw.pl:81/";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String ERROR = "error";
    public static final String URL_LOGIN_PREFERENCE = "url_login_preference";
    public static final String URL_REGISTER_PREFERENCE = "url_register_preference";
    public static final String URL_VERIFY_PREFERENCE = "url_verify_preference";
    public static final String URL_ELECTION_RESULT_PREFERENCE  = "url_election_result_preference";
    public static final String KEY_ALIAS = "openpkw_key";
    public static final String RSA = "RSA";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String SCOPE = "read write";
    public static final String QR = "qr";
    public static final String DATA = "data";
    public static final String LOGIN_FRAGMENT_TAG = "LOGIN_FRAGMENT_TAG";
    public static final String SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT_TAG";
    public static final String ABOUT_FRAGMENT_TAG = "ABOUT_FRAGMENT_TAG";
    public static final String PRESTORE_FRAGMENT_TAG = "PRESTORE_FRAGMENT_TAG";
    public static final String REGISTER_USER_FRAGMENT_TAG = "REGISTER_USER_FRAGMENT_TAG";
    public static final String SCAN_QR_FRAGMENT_TAG = "SCAN_QR_FRAGMENT_TAG";
    public static final String ELECTION_RESULT_FRAGMENT_TAG = "ELECTION_RESULT_FRAGMENT_TAG";
    public static final String VOTING_FORM_FRAGMENT_TAG = "VotingFormFragment";
    public static final String SERVER_RESPONSE = "SERVER RESPONSE: ";
    public static final String OAUTH2_PREFERENCE = "oauth2_preference";
    public static final String OAUTH2_ID_PREFERENCE = "oauth2_id_preference";
    public static final String OAUTH2_SECRET_PREFERENCE = "oauth2_secret_preference";
    public static final String ID_DEFAULT = "openpkw";
    public static final String SECRET_DEFAULT = "secret";
    public static final String DEFAULT_PARAM_CHANGE = "default_param_change";
    public static final String PRIVATE_KEY = "private_key";
    public static final String PUBLIC_KEY = "public_key";
    public static final String PROVIDER_OPEN_SSL = "AndroidOpenSSL";
    public static final String CHARACTER_ENCODING = "UTF-8";
    public static final String ENCRYPTION_MODE_RSA = "RSA/ECB/PKCS1Padding";
    public static final String ENCRYPTION_MODE_AES = "AES/CBC/PKCS5Padding";
    public static final String SIGNATURE_INSTANCE = "SHA256withECDSA";
    public static final String ECDSA = "ECDSA";
    public static final String SECURITY_PROVIDER = "SC";
    public static final String CURVE = "secp256k1";
    public static final String ECIES = "ECIES";
    public static final String AES = "AES";
    public static final String TIMEOUT = "timeout";

    public static final int RSA_KEY_SIZE = 4096;
    public static final int SESSION_TIMER = 60*1000*15-1000;//14 min 59 sec

    public static final int DIALOG_STYLE = android.R.style.Theme_DeviceDefault_Dialog;

    public static boolean isEmailValid(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
