package pl.openpkw.openpkwmobile.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.ElectionCommitteeVotesFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

public class ElectionCommitteeVotesActivity extends AppCompatActivity implements ElectionCommitteeVotesFragment.OnFragmentInteractionListener{

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_committee_votes);

        Bundle extras = getIntent().getExtras();
        Integer listNumber;
        Integer numberOfVotes;
        String nameElectionCommittee = "KOMITET WYBORCZY";
        String addressElectionCommittee = "Adres";
        String wwwAddressElectionCommittee = "Adres WWW";
        if (extras != null) {
            listNumber = extras.getInt(Utils.LIST_NUMBER,0);
            numberOfVotes = extras.getInt(Utils.ELECTION_COMMITTEE_NUMBER_OF_VOTES,0);
            nameElectionCommittee = extras.getString(Utils.ELECTION_COMMITTEE_NAME,"KOMITET WYBORCZY");
            addressElectionCommittee = extras.getString(Utils.ELECTION_COMMITTEE_ADDRESS,"Adres");
            wwwAddressElectionCommittee = extras.getString(Utils.ELECTION_COMMITTEE_WWW_ADDRESS,"Adres WWW");
            Log.e(Utils.TAG,"LIST NUMBER: "+ listNumber);
        }else {
            listNumber = 0;
            numberOfVotes = 0;
        }

        FragmentManager fm = getFragmentManager();
        ElectionCommitteeVotesFragment electionCommitteeVotesFragment = (ElectionCommitteeVotesFragment)  fm.findFragmentByTag(Utils. ELECTION_COMMITTEE_VOTES_FRAGMENT_TAG );
        if (electionCommitteeVotesFragment == null) {

            Bundle bundle = new Bundle();
            bundle.putInt(Utils.LIST_NUMBER, listNumber);
            bundle.putInt(Utils.ELECTION_COMMITTEE_NUMBER_OF_VOTES, numberOfVotes);
            bundle.putString(Utils.ELECTION_COMMITTEE_NAME,nameElectionCommittee);
            bundle.putString(Utils.ELECTION_COMMITTEE_ADDRESS, addressElectionCommittee);
            bundle.putString(Utils.ELECTION_COMMITTEE_WWW_ADDRESS, wwwAddressElectionCommittee);

            electionCommitteeVotesFragment = new ElectionCommitteeVotesFragment();
            electionCommitteeVotesFragment.setArguments(bundle);

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.election_committee_votes_fragment_container, electionCommitteeVotesFragment, Utils. ELECTION_COMMITTEE_VOTES_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }

        //set title and subtitle to action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("Krok 6 z 9");
            actionBar.setSubtitle("Liczba głosów oddanych na kandydatów");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        //set session timer
        MenuItem timerMenuItem = menu.findItem(R.id.session_timer);
        TextView sessionTimerTextView = (TextView) MenuItemCompat.getActionView(timerMenuItem);
        sessionTimerTextView.setPadding(10, 0, 10, 0);
        sessionTimerTextView.setText(timer.getTimer());
        timer.setTimeTextView(sessionTimerTextView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        //hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        //begin transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Create new fragment and transaction
                Fragment settingsFragment = new SettingsFragment();
                transaction.replace(android.R.id.content, settingsFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            case R.id.about_project:
                Fragment aboutFragment = new AboutFragment();
                transaction.replace(android.R.id.content, aboutFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();
            //show main fragment
            getFragmentManager().popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                timer.cancel();
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.fragment_login_twotaptoexit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
