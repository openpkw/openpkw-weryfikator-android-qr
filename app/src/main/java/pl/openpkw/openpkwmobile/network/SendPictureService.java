package pl.openpkw.openpkwmobile.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;

import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.ThumbnailsFragment.getCommitteeProtocolStorageDir;
import static pl.openpkw.openpkwmobile.utils.Utils.STORAGE_PROTOCOL_DIRECTORY;

public class SendPictureService extends IntentService {

    private Handler mHandler;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    final int id = 1;

    public SendPictureService() {
        super("SendPictureService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("OpenPKW")
                .setContentText("Przesyłanie zdjęć w toku")
                .setSmallIcon(android.R.drawable.stat_sys_upload);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int incr;
                for (incr = 0; incr <= 100; incr+=5) {
                    // Sets the progress indicator to a max value, the
                    // current completion percentage, and "determinate"
                    // state
                    mBuilder.setProgress(100, incr, false);
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(id, mBuilder.build());
                    // Sleeps the thread, simulating an operation
                    // that takes time
                    try {
                        // Sleep for 2.5 seconds
                        Thread.sleep(5*50);
                    } catch (InterruptedException e) {
                        Log.d(Utils.TAG, "sleep failure");
                    }
                }
                // When the loop is finished, updates the notification
                mBuilder.setContentText("Zdjęcia zostały przesłane na serwer")
                        // Removes the progress bar
                        .setProgress(0,0,false);
                mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                mNotifyManager.notify(id, mBuilder.build());
                deletePictures();
            }
        });
    }

    private void deletePictures() {
        File[] photoFiles = getCommitteeProtocolStorageDir(STORAGE_PROTOCOL_DIRECTORY).listFiles();
        for(File photoFile : photoFiles){
            photoFile.delete();
        }
    }
}
