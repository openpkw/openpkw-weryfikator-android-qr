package pl.openpkw.openpkwmobile.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InstructionScanQrFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InstructionScanQrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructionScanQrFragment extends Fragment implements View.OnClickListener{


    private OnFragmentInteractionListener mListener;

    public InstructionScanQrFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static InstructionScanQrFragment newInstance() {
        return new InstructionScanQrFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View instructionView = inflater.inflate(R.layout.fragment_instruction_scan_qr, container, false);
        Button forwardButton = (Button) instructionView.findViewById(R.id.instruction_forward_button);
        forwardButton.setOnClickListener(this);
        return instructionView;
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

    @Override
    public void onClick(View view) {
        Intent scanQrIntent = new Intent(getActivity(), ScanQrCodeActivity.class);
        startActivity(scanQrIntent);
        getActivity().finish();
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
