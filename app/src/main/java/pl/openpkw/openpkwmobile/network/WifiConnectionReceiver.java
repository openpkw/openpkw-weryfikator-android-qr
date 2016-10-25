package pl.openpkw.openpkwmobile.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.IS_DATA_SEND;
import static pl.openpkw.openpkwmobile.utils.Utils.TAG;

public class WifiConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = context.getSharedPreferences(DATA, Context.MODE_PRIVATE);
        boolean isDataSend = sharedPref.getBoolean(IS_DATA_SEND,true);
        if(!isDataSend) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isConnected()) {
                        //check is wifi connection
                        String networkTypeName = info.getTypeName();
                        Log.e(TAG, "NETWORK TYPE NAME: " + networkTypeName);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(IS_DATA_SEND, true);
                        editor.apply();

                        Log.e(TAG, "START SERVICE TO SEND PICTURE: ");
                        Intent intentService = new Intent(context, SendPictureService.class);
                        context.startService(intentService);
                    }
                }
            }
        }
    }
}
