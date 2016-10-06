package pl.openpkw.openpkwmobile;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.uiautomator.UiDevice;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import pl.openpkw.openpkwmobile.activities.LoginActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static pl.openpkw.openpkwmobile.network.NetworkUtils.isNetworkAvailable;


public class LoginActivityInstrumentationTest {

    final String email = "test20@openpkw.pl";
    final String incorrectEmail = "incorrectemail@openpkw.pl";
    final String invalidEmail = "openpkw.pl";
    final String password = "password";
    final String emptyEmailError = "Proszę wprowadzić e-mail";
    final String invalidEmailError = "Adres e-mail jest niepoprawny";
    final String emptyPasswordError ="Proszę wprowadzić hasło";
    Activity testActivity;

    @Rule
    public IntentsTestRule<LoginActivity> intentsTestRule =
            new IntentsTestRule<>(LoginActivity.class);

    @Before
    public void checkInternetConnection() throws Exception {
        testActivity = intentsTestRule.getActivity();
        if(!isNetworkAvailable(testActivity))
            throw new Exception("Network connection not available. Enable mobile data/wifi");

        int [] size = getDisplaySize(testActivity);
        wakeUpDevice(size);
    }

    @Test
    public void loginSuccessfully(){
        onView(withId(R.id.login_edittext_user)).perform(typeText(email)).check(matches(withText(email)));
        onView(withId(R.id.login_edittext_password)).perform(typeText(password),closeSoftKeyboard()).check(matches(withText(password)));
        onView(withId(R.id.login_button_login)).perform(click());
        intended(hasComponent(ScanQrCodeActivity.class.getName()));
    }

    @Test
    public void loginFailed(){
        onView(withId(R.id.login_edittext_user)).perform(typeText(incorrectEmail)).check(matches(withText(incorrectEmail)));
        onView(withId(R.id.login_edittext_password)).perform(typeText(password),closeSoftKeyboard()).check(matches(withText(password)));
        onView(withId(R.id.login_button_login)).perform(click());
        //check toast is displayed
        onView(withText(R.string.login_toast_authorization_failed)).
                inRoot(withDecorView(not(is(testActivity.getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void emailIsEmpty() {
        onView(withId(R.id.login_edittext_user)).perform(clearText());
        onView(withId(R.id.login_button_login)).perform(click());
        onView(withId(R.id.login_edittext_user)).check(matches(withError(emptyEmailError)));
    }

    @Test
    public void emailIsInvalid() {
        onView(withId(R.id.login_edittext_user)).perform(typeText(invalidEmail)).check(matches(withText(invalidEmail)));
        onView(withId(R.id.login_button_login)).perform(click());
        onView(withId(R.id.login_edittext_user)).check(matches(withError(invalidEmailError)));
    }

    @Test
    public void passwordIsEmpty(){
        onView(withId(R.id.login_edittext_password)).perform(clearText());
        onView(withId(R.id.login_button_login)).perform(click());
        onView(withId(R.id.login_edittext_password)).check(matches(withError(emptyPasswordError)));
    }

    private static Matcher withError(final String expected) {
        return new TypeSafeMatcher() {
            @Override
            protected boolean matchesSafely(Object item) {
                return item instanceof EditText && ((EditText) item).getError().toString().equals(expected);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Not found error message [" + expected + "]");
            }
        };
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