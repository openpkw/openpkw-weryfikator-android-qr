package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EndFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EndFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EndFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public EndFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EndFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EndFragment newInstance(String param1, String param2) {
        EndFragment fragment = new EndFragment();
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
        View endView = inflater.inflate(R.layout.fragment_end, container, false);

        Button quitButton = (Button) endView.findViewById(R.id.fragment_end_button_quit);
        quitButton.setOnClickListener(quitButtonClickListener);

        Button resultButton = (Button) endView.findViewById(R.id.fragment_end_button_result);
        resultButton.setOnClickListener(resultButtonClickListener);

        return endView;
    }

    View.OnClickListener resultButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getElectionResultUrl()));
            startActivity(browserIntent);

            getActivity().finish();
        }
    };

    View.OnClickListener quitButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getActivity().finish();
        }
    };

    private String getElectionResultUrl(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        return sharedPref.getString(Utils.URL_ELECTION_RESULT_PREFERENCE, Utils.URL_DEFAULT_ELECTION_RESULT).trim();
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
