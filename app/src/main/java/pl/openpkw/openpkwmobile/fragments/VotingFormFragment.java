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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.CommitteesResultActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.qr.QrWrapper;
import pl.openpkw.openpkwmobile.utils.Utils;

public class VotingFormFragment extends Fragment {

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;
    private TextView totalEntitledToVoteTextView;
    private TextView totalVotingCardsTextView;
    private TextView validCardsTextView;
    private TextView invalidVotesTextView;
    private TextView validVotesTextView;

    private List<String> spinnerData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_voting_form, container, false);

        peripheryNumberTextView = (TextView) v.findViewById(R.id.fvoting_periphery_number);
        territorialCodeTextView= (TextView) v.findViewById(R.id.fvoting_territorial_code);
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

        Spinner protocolDataSpinner = (Spinner) v.findViewById(R.id.fvoting_committee_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);

        return v;
    }

    AdapterView.OnItemSelectedListener spinnerItemListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            adapterView.setSelection(0);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

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
        String districtNumber = sharedPref.getString(Utils.DISTRICT_NUMBER, "Okręg Wyborczy Nr");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0 ,territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        spinnerData.add(getString(R.string.committee_label));
        spinnerData.add(periphery_name);
        spinnerData.add(periphery_address);
        spinnerData.add(districtNumber);

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
}
