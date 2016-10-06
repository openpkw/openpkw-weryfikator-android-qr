package pl.openpkw.openpkwmobile;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.uiautomator.UiDevice;
import android.view.Display;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.QR;

public class ScanQrActivityInstrumentationTest {

    private final String qrCodeData = "146519,332,19,2086,1760,103,1657,1,18,0,0,0,0,0,0,0,1656,0,0,1656,12,1644," +
            ",0101;272,0102;38,0103;16,0104;6,0105;8,0106;6,0107;7,0108;4,0109;2,0110;4,0111;4,0112;7,0113;3,0114;17," +
            "0115;4,0116;1,0119;3,0120;4,0121;2,0124;1,0128;2,0129;1,0130;1,0131;5,0132;1,0133;7,0137;3,0138;2,0139;1," +
            "0140;2,0201;366,0202;31,0203;21,0204;12,0205;7,0206;1,0207;33,0208;2,0209;6,0210;3,0211;3,0212;5,0213;2," +
            "0214;2,0218;2,0219;3,0220;1,0222;1,0223;14,0224;1,0227;1,0229;1,0235;1,0236;1,0237;1,0238;1,0240;5,0301;" +
            "78,0302;2,0303;1,0304;4,0305;1,0306;1,0310;2,0320;1,0326;1,0401;58,0402;3,0403;8,0404;1,0412;1,0420;1,0421;" +
            "1,0424;1,0426;1,0501;50,0504;5,0505;2,0510;1,0511;2,0512;1,0601;249,0602;13,0603;1,0605;2,0609;1,0610;7," +
            "0611;2,0612;1,0614;4,0615;1,0616;3,0617;3,0621;1,0623;1,0626;1,0628;1,0629;1,0632;1,0635;1,0636;1,0637;2,0639;2,0701;1,0702;1";

    private final String invalidQrCode = "www.openpkw.org";

    Activity testActivity;

    @Before
    public void getTestActivity(){
        testActivity = intentsTestRule.getActivity();
        clearQrCodeFromSharedPreferences();
        int [] size = getDisplaySize(testActivity);
        wakeUpDevice(size);
    }

    @Rule
    public IntentsTestRule<ScanQrCodeActivity> intentsTestRule =
            new IntentsTestRule<>(ScanQrCodeActivity.class);

    @Test
    public void qrCodeScanSuccessfully(){

        // Build a result to return from the ZXING app
        Intent resultData = new Intent();
        resultData.putExtra("SCAN_RESULT", qrCodeData);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // When an intent is sent to the ZXING app, this tells Espresso to respond
        // with the ActivityResult we just created
        intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);

        // Now that we have the stub in place, click on the button in our app that launches into the ZXING app
        onView(withId(R.id.scan_qr_button_scan)).perform(click());

        //check toast is displayed
        onView(withText(R.string.toast_scanned_qr_ok)).
                inRoot(withDecorView(not(is(testActivity.getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void invalidQrCodeScan(){
        Intent resultData = new Intent();
        resultData.putExtra("SCAN_RESULT", invalidQrCode);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);
        onView(withId(R.id.scan_qr_button_scan)).perform(click());
        //check is dialog incorrect qr code is displayed
        onView(withText(R.string.dialog_incorrect_qr_message))
                .inRoot(isDialog()) // <---
                .check(matches(isDisplayed()));
    }

    @Test
    public void qrCodeScanFailed(){
        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData);
        intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);
        onView(withId(R.id.scan_qr_button_scan)).perform(click());
        //check is dialog failed scan qr code is displayed
        onView(withText(R.string.dialog_scan_qr_failed))
                .inRoot(isDialog()) // <---
                .check(matches(isDisplayed()));
    }

    private void clearQrCodeFromSharedPreferences() {
        SharedPreferences sharedPreferences = getInstrumentation().getTargetContext().
                getSharedPreferences(DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(QR, null);
        editor.apply();
    }

    private void wakeUpDevice(int [] size) {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Point[] coordinates = new Point[4];
        coordinates[0] = new Point(248, (int)Math.round(size[1]*0.8));
        coordinates[1] = new Point(248, (int)Math.round(size[1]*0.5));
        coordinates[2] = new Point((int)Math.round(size[0]*0.8), (int)Math.round(size[1]*0.8));
        coordinates[3] = new Point((int)Math.round(size[0]*0.8), (int)Math.round(size[1]*0.5));
        try {
            if (!uiDevice.isScreenOn()) {
                uiDevice.wakeUp();
                uiDevice.swipe(coordinates, 10);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int [] getDisplaySize(Activity activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int [] displaySize = new int[2];
        displaySize[0] = size.x;
        displaySize[1] = size.y;
        return displaySize;
    }
}
