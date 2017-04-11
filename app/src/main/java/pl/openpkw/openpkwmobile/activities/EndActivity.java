package pl.openpkw.openpkwmobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.EndFragment;

import static pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity.showToast;
import static pl.openpkw.openpkwmobile.utils.Utils.END_FRAGMENT_TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.SERVER_RESPONSE;

public class EndActivity extends AppCompatActivity implements EndFragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        if(savedInstanceState==null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.end_fragment_container, new EndFragment(), END_FRAGMENT_TAG)
                    .commit();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            String response =bundle.getString(SERVER_RESPONSE,null);
            Toast.makeText(this,response,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showToast(R.string.fragment_login_twotaptoexit,this,false);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
