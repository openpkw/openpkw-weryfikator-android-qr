package pl.openpkw.openpkwmobile.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import pl.openpkw.openpkwmobile.activities.TimeoutDialogActivity;

public final class TimerSingleton extends CountDownTimer{

    public static final String TIMEOUT = "00:00";

    private TextView timeTextView;
    private String timer;
    private Context context;
    long min;
    long sec;

    public TimerSingleton(long millisInFuture, long countDownInterval, Context context) {
        super(millisInFuture, countDownInterval);
        this.context = context;
    }

    @Override
    public void onTick(long left) {
        min = (TimeUnit.MILLISECONDS.toMinutes(left) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(left)));

        sec = TimeUnit.MILLISECONDS.toSeconds(left)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(left));

        String minStr;
        String secStr;

        if(min<10)
            minStr = "0" + min;
        else
            minStr = "" + min;

        if(sec<10)
            secStr = "0" + sec;
        else
            secStr = "" + sec;

        timer = minStr + ":" + secStr;

        if(timeTextView!=null)
            timeTextView.setText(timer);
    }

    @Override
    public void onFinish() {
        if(timeTextView!=null)
            timeTextView.setText(TIMEOUT);

        TimeoutDialogActivity.createDialog(context);
    }

    public void setTimeTextView(TextView timeTextView) {
        this.timeTextView = timeTextView;
    }

    public String getTimer() {
        return timer;
    }

}
