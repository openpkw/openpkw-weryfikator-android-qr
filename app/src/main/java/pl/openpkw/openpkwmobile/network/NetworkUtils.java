package pl.openpkw.openpkwmobile.network;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.util.Base64;


public class NetworkUtils {

    private static final String BASIC_AUTHENTICATION = "Basic ";

    public static boolean isNetworkAvailable(Activity activity) {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)activity.getSystemService(activity.getBaseContext().CONNECTIVITY_SERVICE);
        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||

                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            return false;
        }
        return false;
    }

    public static String base64EncodedBasicAuthentication(String id, String secret) {
        String basicAuth = id+":"+secret;
        String base64 = Base64.encodeToString(basicAuth.getBytes(), Base64.DEFAULT);
        return BASIC_AUTHENTICATION + base64;
    }
}
