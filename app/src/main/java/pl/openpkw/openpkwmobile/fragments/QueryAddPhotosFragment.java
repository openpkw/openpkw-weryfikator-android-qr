package pl.openpkw.openpkwmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import pl.openpkw.openpkwmobile.activities.SendDataActivity;
import pl.openpkw.openpkwmobile.camera.CameraActivity;
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

    private OnFragmentInteractionListener mListener;
    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    public QueryAddPhotosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment QueryAddPhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QueryAddPhotosFragment newInstance() {
        return new QueryAddPhotosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        territorialCodeTextView = (TextView) queryAddPhotosView.findViewById(R.id.query_add_territorial_code);
        peripheryNumberTextView = (TextView) queryAddPhotosView.findViewById(R.id.query_add_periphery_number);

        loadData();

        return queryAddPhotosView;
    }

    private void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String territorial_code = "  "+sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny")+"  ";
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN),0, territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
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
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                onButtonPressed(null);
            else{
                Activity activity = getActivity();
                Intent cameraIntent = new Intent(activity , CameraActivity.class);
                startActivity(cameraIntent);
                activity.finish();
            }
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
