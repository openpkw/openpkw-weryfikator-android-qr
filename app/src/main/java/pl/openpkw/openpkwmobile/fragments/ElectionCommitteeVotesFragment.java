package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.CommitteesResultActivity;
import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity.candidatesHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ElectionCommitteeVotesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ElectionCommitteeVotesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ElectionCommitteeVotesFragment extends Fragment {

    private TableLayout candiddatesVotesNumberLayout;
    private List<String> spinnerData = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public ElectionCommitteeVotesFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ElectionCommitteeVotesFragment newInstance() {
        return new ElectionCommitteeVotesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View electionCommitteeView = inflater.inflate(R.layout.fragment_election_committee_votes, container, false);
        candiddatesVotesNumberLayout = (TableLayout) electionCommitteeView.findViewById(R.id.candidates_votes_number_layout);

        Button forwardButton = (Button) electionCommitteeView.findViewById(R.id.election_committee_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        TextView listNumberTextView = (TextView) electionCommitteeView.findViewById(R.id.election_committee_list_number_label);

        Integer listNumber = getArguments().getInt(Utils.LIST_NUMBER,0);
        String listNumberStr = "Lista nr " + "  "+listNumber+"  ";

        Spannable spannableGreen = new SpannableString(listNumberStr );
        spannableGreen .setSpan(new ForegroundColorSpan(Color.GREEN), "Lista nr ".length() ,listNumberStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        listNumberTextView.setText(spannableGreen);

        loadData(listNumber);

        Spinner protocolDataSpinner = (Spinner) electionCommitteeView.findViewById(R.id.election_committee_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);

        return electionCommitteeView;
    }

    AdapterView.OnItemSelectedListener spinnerItemListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            adapterView.setSelection(0);
            if(i==2) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(adapterView.getItemAtPosition(i).toString()));
                startActivity(browserIntent);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent committeesResultIntent = new Intent(getActivity(), CommitteesResultActivity.class);
            startActivity(committeesResultIntent);
            getActivity().finish();
        }
    };

    private void loadData(int listNumber) {
        int totalNumberOfVotes = getArguments().getInt(Utils.ELECTION_COMMITTEE_NUMBER_OF_VOTES, 0);
        String nameElectionCommittee = getArguments().getString(Utils.ELECTION_COMMITTEE_NAME,"KOMITET WYBORCZY");
        String addressElectionCommittee = getArguments().getString(Utils.ELECTION_COMMITTEE_ADDRESS,"Adres");
        String wwwAddressElectionCommittee = getArguments().getString(Utils.ELECTION_COMMITTEE_WWW_ADDRESS,"Adres WWW");
        String totalVotesStr = "Łączna liczba uzyskanych głosów: "+String.valueOf(totalNumberOfVotes);
        spinnerData.add(nameElectionCommittee);
        spinnerData.add(addressElectionCommittee);
        spinnerData.add(wwwAddressElectionCommittee );
        spinnerData.add(totalVotesStr);

        HashMap<String,CandidateVoteDTO> candidatesMap = candidatesHashMap;
        Iterator it = candidatesMap.entrySet().iterator();
        LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String numberOfVotes;
        Map.Entry pair;
        CandidateVoteDTO candidate;
        while (it.hasNext()) {
            pair = (Map.Entry)it.next();
            candidate = (CandidateVoteDTO)pair.getValue();
            if(candidate.getListNumber()==listNumber)
            {
                TableRow candidatesRow = (TableRow) layoutInflater.inflate(R.layout.candidate_tablerow,null);
                TableRow candidatesSeparatorRow = (TableRow) layoutInflater.inflate(R.layout.committee_separator_tablerow,null);
                TextView committeeNameTextView = (TextView) candidatesRow.findViewById(R.id.tablerow_candidate_name);
                TextView committeeNumberOfVotesTextView = (TextView) candidatesRow.findViewById(R.id.tablerow_candidate_number_of_votes);

                numberOfVotes = String.valueOf(candidate.getVotesNumber());
                Spannable spannable = new SpannableString(numberOfVotes);
                spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0 ,numberOfVotes.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                committeeNameTextView.setText(candidate.getName());
                committeeNumberOfVotesTextView.setText(spannable);
                candiddatesVotesNumberLayout.addView(candidatesSeparatorRow);
                candiddatesVotesNumberLayout.addView(candidatesRow);
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
