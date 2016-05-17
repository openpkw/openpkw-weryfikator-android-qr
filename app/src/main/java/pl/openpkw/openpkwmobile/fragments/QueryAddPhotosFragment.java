package pl.openpkw.openpkwmobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.AddPhotosActivity;
import pl.openpkw.openpkwmobile.activities.CommitteesResultActivity;
import pl.openpkw.openpkwmobile.activities.SendDataActivity;
import pl.openpkw.openpkwmobile.activities.VotingFormActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QueryAddPhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QueryAddPhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QueryAddPhotosFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public QueryAddPhotosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QueryAddPhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QueryAddPhotosFragment newInstance(String param1, String param2) {
        QueryAddPhotosFragment fragment = new QueryAddPhotosFragment();
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
        View queryAddPhotosView = inflater.inflate(R.layout.fragment_query_add_photos, container, false);

        Button forwardButton = (Button) queryAddPhotosView.findViewById(R.id.query_add_photos_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        Button yesButton = (Button) queryAddPhotosView.findViewById(R.id.query_add_photos_yes_button);
        yesButton.setOnClickListener(yesButtonClickListener);

        Button noButton = (Button) queryAddPhotosView.findViewById(R.id.query_add_photos_no_button);
        noButton.setOnClickListener(noButtonClickListener);

        return queryAddPhotosView;
    }

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent committeesResultIntent = new Intent(getActivity(), CommitteesResultActivity.class);
            startActivity(committeesResultIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener yesButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent addPhotosIntent = new Intent(getActivity(), AddPhotosActivity.class);
            startActivity(addPhotosIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener noButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent sendDataIntent = new Intent(getActivity(),SendDataActivity.class);
            startActivity(sendDataIntent);
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