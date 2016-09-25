package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import java.util.HashSet;
import java.util.List;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.ElectionCommitteeVotesActivity;
import pl.openpkw.openpkwmobile.activities.QueryAddPhotosActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.activities.VotingFormActivity;
import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.models.ElectionCommitteeDTO;
import pl.openpkw.openpkwmobile.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CommitteesResultFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CommitteesResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommitteesResultFragment extends Fragment {

    TableLayout committeesResultLayout;
    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    private List<String> spinnerData = new ArrayList<>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

   // private HashMap<String,Integer> electionCommitteesMap;

    public static HashMap<String,CandidateVoteDTO> candidatesMap;

    public static HashMap<String,ElectionCommitteeDTO> electionCommitteeMap;

    private OnFragmentInteractionListener mListener;

    public CommitteesResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CommitteesResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CommitteesResultFragment newInstance(String param1, String param2) {
        CommitteesResultFragment fragment = new CommitteesResultFragment();
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
        View committeesResultView = inflater.inflate(R.layout.fragment_committees_result, container, false);
        committeesResultLayout = (TableLayout) committeesResultView.findViewById(R.id.committees_result_layout);

        peripheryNumberTextView = (TextView) committeesResultView.findViewById(R.id.committees_result_periphery_number);
        territorialCodeTextView= (TextView) committeesResultView.findViewById(R.id.committees_result_territorial_code);

        Button nextButton = (Button) committeesResultView.findViewById(R.id.committees_result_next_button);
        nextButton.setOnClickListener(nextButtonClickListener);

        Button forwardButton = (Button) committeesResultView.findViewById(R.id.committees_result_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        loadData();

        Spinner protocolDataSpinner = (Spinner) committeesResultView.findViewById(R.id.committees_result_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);
        return committeesResultView;
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
            Intent queryAddPhotosIntent = new Intent(getActivity(), QueryAddPhotosActivity.class);
            startActivity(queryAddPhotosIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent votingFormIntent = new Intent(getActivity(), VotingFormActivity.class);
            startActivity(votingFormIntent);
            getActivity().finish();
        }
    };

    private void loadData(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String scannedQR = sharedPref.getString(Utils.QR,null);
        String territorial_code = "  "+sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny")+"  ";
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        String periphery_name = sharedPref.getString(Utils.PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(Utils.PERIPHERY_ADDRESS, "Adres");
        String districtNumber = sharedPref.getString(Utils.DISTRICT_NUMBER, "Okręg Wyborczy Nr");
        Spannable spannableGreen = new SpannableString(territorial_code);
        spannableGreen.setSpan(new ForegroundColorSpan(Color.GREEN),0, territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannableGreen);
        peripheryNumberTextView.setText(periphery_number);
        spinnerData.add(getString(R.string.committee_label));
        spinnerData.add(periphery_name);
        spinnerData.add(periphery_address);
        spinnerData.add(districtNumber);

        if(scannedQR!=null) {
            candidatesMap = ScanQrCodeActivity.candidatesHashMap;
            HashSet<String> electionCommitteeDistrictList = ScanQrCodeActivity.electionCommitteeDistrictList;
            electionCommitteeMap = ScanQrCodeActivity.electionCommitteeMap;

            LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            String shortNameElectionCommittee = "Komitet Wyborczy";

            String numberOfVotes = "0";

            ElectionCommitteeDTO electionCommittee;

            for (String iElectionCommitteeList : electionCommitteeDistrictList) {
                //inflate committee row
                TableRow committeeRow = (TableRow) layoutInflater.inflate(R.layout.committee_result_tablerow,null);

                electionCommittee = electionCommitteeMap.get(iElectionCommitteeList.substring(
                        0, iElectionCommitteeList.indexOf(";")));

                if(electionCommittee!=null) {
                    shortNameElectionCommittee = electionCommittee.getShortName();
                    electionCommittee.setListNumber(Integer.valueOf(iElectionCommitteeList.substring(iElectionCommitteeList.indexOf(";")+1,iElectionCommitteeList.length())));
                    numberOfVotes = String.valueOf(electionCommittee.getTotalNumberOfVotes());
                    committeeRow.setTag(electionCommittee.getName());
                    electionCommitteeMap.put(electionCommittee.getName(),electionCommittee);
                }

                Spannable spannable = new SpannableString(numberOfVotes);
                spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0 ,numberOfVotes.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                committeeRow.setOnClickListener(committeeRowClickListener);
                TableRow committeeSeparatorRow = (TableRow) layoutInflater.inflate(R.layout.committee_separator_tablerow,null);
                TextView nameTextView = (TextView) committeeRow.findViewById(R.id.committee_tablerow_committee_name);
                TextView numberOfVotesTextView = (TextView) committeeRow.findViewById(R.id.committee_tablerow_committee_number_of_votes);
                nameTextView.setText(shortNameElectionCommittee);
                numberOfVotesTextView.setText(spannable);
                committeesResultLayout.addView(committeeSeparatorRow);
                committeesResultLayout.addView(committeeRow);
            }
        }
    }


    View.OnClickListener committeeRowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView committeeName = (TextView) view.findViewById(R.id.committee_tablerow_committee_name);
            Intent electionCommitteeVotesIntent = new Intent(getActivity(), ElectionCommitteeVotesActivity.class);
            String electionCommitteeName = committeeName.getText().toString();
            Log.e(Utils.TAG,"KW SHORT: "+electionCommitteeName);
            Log.e(Utils.TAG,"LIST NUMBER CLICK: "+view.getTag());
            ElectionCommitteeDTO electionCommittee = electionCommitteeMap.get(String.valueOf(view.getTag()));

            if(electionCommittee!=null)
            {
                electionCommitteeVotesIntent.putExtra(Utils.LIST_NUMBER, electionCommittee.getListNumber());
                electionCommitteeVotesIntent.putExtra(Utils.ELECTION_COMMITTEE_NAME, electionCommittee.getName());
                electionCommitteeVotesIntent.putExtra(Utils.ELECTION_COMMITTEE_ADDRESS, electionCommittee.getAddress());
                electionCommitteeVotesIntent.putExtra(Utils.ELECTION_COMMITTEE_WWW_ADDRESS, electionCommittee.getWwwAddress());
                electionCommitteeVotesIntent.putExtra(Utils.ELECTION_COMMITTEE_NUMBER_OF_VOTES, electionCommittee.getTotalNumberOfVotes());
            }

            startActivity(electionCommitteeVotesIntent);
            getActivity().finish();
        }
    };

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
