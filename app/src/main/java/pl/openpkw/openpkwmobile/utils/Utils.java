package pl.openpkw.openpkwmobile.utils;

import android.text.TextUtils;


public class Utils {

    public static final String TAG = "OPENPKW";
    public static final String URL_DEFAULT__VERIFY_QR = "http://rumcajs.openpkw.pl:9080/openpkw/api/qr";
    public static final String URL_DEFAULT_LOGIN = "http://rumcajs.openpkw.pl:9080/openpkw/api/login";
    public static final String URL_DEFAULT_REGISTER = "http://rumcajs.openpkw.pl:9080/openpkw/users/";
    public static final String URL_DEFAULT_ELECTION_RESULT = "http://rumcajs.openpkw.pl:81/";
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
    public static final String TERRITORIAL_CODE = "TERRITORIAL_CODE";
    public static final String PERIPHERY_NUMBER = "PERIPHERY_NUMBER";
    public static final String DISTRICT_NUMBER = "DISTRICT_NUMBER";
    public static final String PERIPHERY_NAME = "PERIPHERY_NAME";
    public static final String PERIPHERY_ADDRESS = " PERIPHERY_ADDRESS";
    public static final String LOGIN_FRAGMENT_TAG = "LOGIN_FRAGMENT_TAG";
    public static final String NEXT_PHOTO_FRAGMENT_TAG = "NEXT_PHOTO_FRAGMENT_TAG";
    public static final String ADD_PHOTOS_FRAGMENT_TAG = "ADD_PHOTOS_FRAGMENT_TAG";
    public static final String QUERY_ADD_PHOTOS_FRAGMENT_TAG = "QUERY_ADD_PHOTOS_FRAGMENT_TAG";
    public static final String SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT_TAG";
    public static final String ABOUT_FRAGMENT_TAG = "ABOUT_FRAGMENT_TAG";
    public static final String PRESTORE_FRAGMENT_TAG = "PRESTORE_FRAGMENT_TAG";
    public static final String REGISTER_USER_FRAGMENT_TAG = "REGISTER_USER_FRAGMENT_TAG";
    public static final String SCAN_QR_FRAGMENT_TAG = "SCAN_QR_FRAGMENT_TAG";
    public static final String COMMITTEES_RESULT_FRAGMENT_TAG = "COMMITTEES_RESULT_FRAGMENT_TAG";
    public static final String ELECTION_COMMITTEE_VOTES_FRAGMENT_TAG = " ELECTION_COMMITTEE_VOTES_FRAGMENT_TAG";
    public static final String VOTING_FORM_FRAGMENT_TAG = "ELECTION_RESULT_FRAGMENT_TAG";
    public static final String END_FRAGMENT_TAG = "END_FRAGMENT_TAG";
    public static final String THUMBNAILS_FRAGMENT_TAG = "THUMBNAILS_FRAGMENT_TAG ";
    public static final String SEND_DATA_FRAGMENT_TAG = "SEND_DATA_FRAGMENT_TAG ";
    public static final String STORAGE_PROTOCOL_DIRECTORY = "COMMITTEE_PROTOCOL";
    public static final String SERVER_RESPONSE = "SERVER RESPONSE: ";
    public static final String LIST_NUMBER = "LIST_NUMBER";
    public static final String ELECTION_COMMITTEE_NAME = "ELECTION_COMMITTEE_NAME";
    public static final String ELECTION_COMMITTEE_ADDRESS = "ELECTION_COMMITTEE_ADDRESS";
    public static final String ELECTION_COMMITTEE_WWW_ADDRESS = "ELECTION_COMMITTEE_WWW_ADDRESS";
    public static final String ELECTION_COMMITTEE_NUMBER_OF_VOTES = "ELECTION_COMMITTEE_NUMBER_OF_VOTES";
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
    public static final String PATH_TO_PHOTO = "PATH_TO_PHOTO";

    public static final int RSA_KEY_SIZE = 4096;
    public static final int SESSION_TIMER = 60*1000*15-1000;//14 min 59 sec
    //public static final int SESSION_TIMER = 10*1000; //test value
    public static final int DIALOG_STYLE = android.R.style.Theme_DeviceDefault_Dialog;
    public static final int MAX_NUMBER_OF_PHOTOS = 6;
    public static final int PERMISSION_REQUEST_CAMERA = 0;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;


    public static boolean isEmailValid(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
