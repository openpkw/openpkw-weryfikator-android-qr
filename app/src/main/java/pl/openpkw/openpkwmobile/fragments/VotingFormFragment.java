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

import static pl.openpkw.openpkwmobile.fragments.ScanQrCodeFragment.createIndentedText;
import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_ADDRESS;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;

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
        peripheryNameTextView = (TextView)v.findViewById(R.id.fvoting_periphery_name);
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
        peripheryAddressTextView.setText("Adres: ");
        peripheryAddressTextView.measure(0,0);
        int addressLabelTextWidth = peripheryAddressTextView.getMeasuredWidth();
        peripheryNameTextView.setText("Nazwa: ");
        peripheryNameTextView.measure(0,0);
        int peripheryNameLabelTextWidth = peripheryNameTextView.getMeasuredWidth();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
        String scannedQR = sharedPref.getString(Utils.QR,null);
        String territorial_code = sharedPref.getString(TERRITORIAL_CODE, "Kod terytorialny: _ _ _ _");
        if(!territorial_code.equalsIgnoreCase("Kod terytorialny: _ _ _ _"))
            territorial_code = "Kod terytorialny: "+territorial_code;
        String periphery_number = sharedPref.getString(PERIPHERY_NUMBER, "Nr obwodu: _ _ _ _");
        if(!periphery_number.equalsIgnoreCase("Nr obwodu: _ _ _ _"))
            periphery_number = "Nr obwodu: "+periphery_number;
        String periphery_name = sharedPref.getString(PERIPHERY_NAME, "Nazwa: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        if(!periphery_name.equalsIgnoreCase("Nazwa: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _"))
            periphery_name = "Nazwa: " + periphery_name;
        String periphery_address = sharedPref.getString(PERIPHERY_ADDRESS, "Adres: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _");
        if(!periphery_address.equalsIgnoreCase("Adres: _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _"))
            periphery_address = "Adres: "+periphery_address;
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN),"Kod terytorialny: ".length(), territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        peripheryNameTextView.setText(createIndentedText(periphery_name,0,peripheryNameLabelTextWidth ));
        peripheryAddressTextView.setText(createIndentedText(periphery_address,0,addressLabelTextWidth));

        if(scannedQR!=null) {
            QrWrapper qrWrapper = new QrWrapper(scannedQR);
            totalEntitledToVoteTextView.setTextColor(Color.BLUE);
            totalEntitledToVoteTextView.setText(qrWrapper.getVotingCardsTotalEntitledToVote());
            totalVotingCardsTextView.setText(qrWrapper.getVotingCardsTotalCards());
            totalVotingCardsTextView.setTextColor(Color.BLUE);
            validCardsTextView.setText(qrWrapper.getVotingCardsValidCards());
            validCardsTextView.setTextColor(Color.BLUE);
            invalidVotesTextView.setText(qrWrapper.getVotingCardsInvalidVotes());
            invalidVotesTextView.setTextColor(Color.BLUE);
            validVotesTextView.setText(qrWrapper.getVotingCardsValidVotes());
            validVotesTextView.setTextColor(Color.BLUE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
