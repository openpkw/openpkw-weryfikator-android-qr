package pl.openpkw.openpkwmobile.network;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

public class SendPictureService extends IntentService {

    private Handler mHandler;

    public SendPictureService() {
        super("SendPictureService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SendPictureService.this,"Zdjęcia zostały przesłane na serwer",Toast.LENGTH_LONG).show();
            }
        });
    }
}
