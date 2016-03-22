package pl.openpkw.openpkwmobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.VotingFormFragment;
import pl.openpkw.openpkwmobile.utils.Utils;


public class VotingFormActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_form);

        FragmentManager fm = getSupportFragmentManager();
        VotingFormFragment fvFragment = (VotingFormFragment)
                fm.findFragmentByTag(Utils.VOTING_FORM_FRAGMENT_TAG);
        if (fvFragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fvoting_fragment_container, new VotingFormFragment(),
                    Utils.VOTING_FORM_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent scanIntent = new Intent(VotingFormActivity.this, ScanQrCodeActivity.class);
        startActivity(scanIntent);
        finish();
    }
}
