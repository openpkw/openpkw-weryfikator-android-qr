package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.CommitteesResultActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.qr.QrWrapper;
import pl.openpkw.openpkwmobile.utils.Utils;

public class VotingFormFragment extends Fragment {

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;
    private TextView peripheryNameTextView;
    private TextView peripheryAddressTextView;

    private TextView totalEntitledToVoteTextView;
    private TextView totalVotingCardsTextView;
    private TextView validCardsTextView;
    private TextView invalidVotesTextView;
    private TextView validVotesTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_voting_form, container, false);

        peripheryNumberTextView = (TextView) v.findViewById(R.id.fvoting_periphery_number);
        territorialCodeTextView= (TextView) v.findViewById(R.id.fvoting_territorial_code);
        peripheryNameTextView = (TextView) v.findViewById(R.id.fvoting_periphery_name);
        peripheryAddressTextView = (TextView) v.findViewById(R.id.fvoting_periphery_address);

        totalEntitledToVoteTextView = (TextView) v.findViewById(R.id.fvoting_total_entitled_to_vote);
        totalVotingCardsTextView = (TextView) v.findViewById(R.id.fvoting_total_voting_cards);
        validCardsTextView = (TextView) v.findViewById(R.id.fvoting_valid_cards);
        invalidVotesTextView = (TextView) v.findViewById(R.id.fvoting_invalid_votes);
        validVotesTextView = (TextView) v.findViewById(R.id.fvoting_valid_votes);

        Button forwardButton = (Button) v.findViewById(R.id.fvoting_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        Button nextButton = (Button) v.findViewById(R.id.fvoting_next_button);
        nextButton.setOnClickListener(nextButtonClickListener);

        loadData();
        return v;
    }

    View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent committeesResultIntent = new Intent(getActivity(), CommitteesResultActivity.class);
            startActivity(committeesResultIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent scanQrIntent = new Intent(getActivity(), ScanQrCodeActivity.class);
            startActivity(scanQrIntent);
            getActivity().finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String scannedQR = sharedPref.getString(Utils.QR,null);
        String territorial_code = sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny");
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        String periphery_name = sharedPref.getString(Utils.PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(Utils.PERIPHERY_ADDRESS, "Adres");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0 ,territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        peripheryNameTextView.setText(periphery_name);
        peripheryAddressTextView.setText(periphery_address);
        if(scannedQR!=null) {
            QrWrapper qrWrapper = new QrWrapper(scannedQR);
            totalEntitledToVoteTextView.setText(qrWrapper.getVotingCardsTotalEntitledToVote());
            totalVotingCardsTextView.setText(qrWrapper.getVotingCardsTotalCards());
            validCardsTextView.setText(qrWrapper.getVotingCardsValidCards());
            invalidVotesTextView.setText(qrWrapper.getVotingCardsInvalidVotes());
            validVotesTextView.setText(qrWrapper.getVotingCardsValidVotes());
        }
    }
}
