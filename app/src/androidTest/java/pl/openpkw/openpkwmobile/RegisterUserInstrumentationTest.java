package pl.openpkw.openpkwmobile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.view.Display;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

import pl.openpkw.openpkwmobile.activities.RegisterUserActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static pl.openpkw.openpkwmobile.network.NetworkUtils.isNetworkAvailable;

public class RegisterUserInstrumentationTest {

    private final String name = "Jan";
    private final String surname = "Kowalski";
    private final String password = "password";
    final String emailExists = "test20@openpkw.pl";

    @Rule
    public ActivityTestRule<RegisterUserActivity> activityTestRule
            = new ActivityTestRule<>(RegisterUserActivity.class);

    @Before
    public void checkInternetConnection() throws Exception {
        Activity testActivity = activityTestRule.getActivity();
        if(!isNetworkAvailable(testActivity))
            throw new Exception("Network connection not available. Enable mobile data/wifi");
        int [] size = getDisplaySize(testActivity);
        wakeUpDevice(size);
    }

    @Test
    public void registerUserSuccessfully(){
        onView(withId(R.id.register_edittext_name)).perform(typeText(name)).check(matches(withText(name)));
        onView(withId(R.id.register_edittext_surname)).perform(typeText(surname)).check(matches(withText(surname)));
        //generate random email
        String email ="test"+String.valueOf(randInt(100,1000))+"@openpkw.pl";
        onView(withId(R.id.register_edittext_email)).perform(typeText(email)).check(matches(withText(email)));
        onView(withId(R.id.register_edittext_password)).perform(typeText(password),closeSoftKeyboard()).check(matches(withText(password)));
        onView(withId(R.id.register_edittext_password_confirm)).perform(typeText(password),closeSoftKeyboard()).check(matches(withText(password)));
        onView(withId(R.id.register_button_register_user)).perform(click());
        onView(withText(R.string.register_toast_user_register_ok_message)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.session_timeout_login)).perform(click());
    }

    @Test
    public void registerUserFailed_UserExists(){
        onView(withId(R.id.register_edittext_name)).perform(typeText(name)).check(matches(withText(name)));
        onView(withId(R.id.register_edittext_surname)).perform(typeText(surname)).check(matches(withText(surname)));
        onView(withId(R.id.register_edittext_email)).perform(typeText(emailExists)).check(matches(withText(emailExists)));
        onView(withId(R.id.register_edittext_password)).perform(typeText(password),closeSoftKeyboard()).check(matches(withText(password)));
        onView(withId(R.id.register_edittext_password_confirm)).perform(typeText(password),closeSoftKeyboard()).check(matches(withText(password)));
        onView(withId(R.id.register_button_register_user)).perform(click());
        onView(withText(R.string.register_toast_user_already_exist_message)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
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
