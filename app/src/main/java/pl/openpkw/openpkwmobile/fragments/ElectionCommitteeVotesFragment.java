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
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.CommitteesResultActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.activities.VotingFormActivity;
import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.utils.Utils;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ElectionCommitteeVotesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ElectionCommitteeVotesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ElectionCommitteeVotesFragment newInstance(String param1, String param2) {
        ElectionCommitteeVotesFragment fragment = new ElectionCommitteeVotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View electionCommitteeView = inflater.inflate(R.layout.fragment_election_committee_votes, container, false);
        candiddatesVotesNumberLayout = (TableLayout) electionCommitteeView.findViewById(R.id.candidates_votes_number_layout);

        Button forwardButton = (Button) electionCommitteeView.findViewById(R.id.election_committee_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        TextView listNumberTextView = (TextView) electionCommitteeView.findViewById(R.id.election_committee_votes_label_top);
        TextView electionCommitteeNameTextView = (TextView) electionCommitteeView.findViewById(R.id.election_committee_name);
        TextView addressElectionCommitteeTextView = (TextView) electionCommitteeView.findViewById(R.id.election_committee_address);
        TextView wwwAddressElectionCommitteeTextView = (TextView) electionCommitteeView.findViewById(R.id.election_committee_www_address);
        TextView totalVotesTextView = (TextView) electionCommitteeView.findViewById(R.id.election_committee_total_number_of_votes);

        Integer listNumber = getArguments().getInt(Utils.LIST_NUMBER,0);
        int totalNumberOfVotes = getArguments().getInt(Utils.ELECTION_COMMITTEE_NUMBER_OF_VOTES, 0);
        String nameElectionCommittee = getArguments().getString(Utils.ELECTION_COMMITTEE_NAME,"KOMITET WYBORCZY");
        String addressElectionCommittee = getArguments().getString(Utils.ELECTION_COMMITTEE_ADDRESS,"Adres");
        String wwwAddressElectionCommittee = getArguments().getString(Utils.ELECTION_COMMITTEE_WWW_ADDRESS,"Adres WWW");

        listNumberTextView.setText("Lista nr "+listNumber);
        electionCommitteeNameTextView.setText(nameElectionCommittee);
        addressElectionCommitteeTextView.setText(addressElectionCommittee);
        wwwAddressElectionCommitteeTextView.setText(wwwAddressElectionCommittee);
        String totalVotesStr = "Łączna liczba uzyskanych głosów: "+String.valueOf(totalNumberOfVotes);
        totalVotesTextView.setText(totalVotesStr);

        loadData(listNumber);

        return electionCommitteeView;
    }

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent committeesResultIntent = new Intent(getActivity(), CommitteesResultActivity.class);
            startActivity(committeesResultIntent);
            getActivity().finish();
        }
    };

    private void loadData(int listNumber) {
        HashMap<String,CandidateVoteDTO> candidatesMap = ScanQrCodeActivity.candidatesHashMap;
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
