package pl.openpkw.openpkwmobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.NextPhotoActivity;
import pl.openpkw.openpkwmobile.camera.CameraActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RetryPhotoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RetryPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RetryPhotoFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView photoImageView;

    private String mCurrentPhotoPath;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    private List<String> spinnerData = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public RetryPhotoFragment() {
        // Required empty public constructor
    }

    public static NextPhotoFragment newInstance() {
        return new NextPhotoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retryPhotoView = inflater.inflate(R.layout.fragment_retry_photo, container, false);
        photoImageView = (ImageView) retryPhotoView.findViewById(R.id.retry_photo_image_view_photo);

        Button nextPhotoButton = (Button) retryPhotoView.findViewById(R.id.retry_photo_next_button);
        nextPhotoButton.setOnClickListener(nextButtonClickListener);

        Button retryButton = (Button) retryPhotoView.findViewById(R.id.retry_photo_retry_button);
        retryButton.setOnClickListener(retryButtonClickListener);

        territorialCodeTextView = (TextView) retryPhotoView.findViewById(R.id.retry_photo_territorial_code);
        peripheryNumberTextView = (TextView) retryPhotoView.findViewById(R.id.retry_photo_periphery_number);

        loadData();

        Spinner protocolDataSpinner = (Spinner) retryPhotoView.findViewById(R.id.retry_photo_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);

        return retryPhotoView;
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

    private void loadData(){
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String territorial_code = sharedPref.getString(TERRITORIAL_CODE, "Kod terytorialny: _ _ _ _");
        if(!territorial_code.equalsIgnoreCase("Kod terytorialny: _ _ _ _"))
            territorial_code = "Kod terytorialny: "+territorial_code;
        String periphery_number = sharedPref.getString(PERIPHERY_NUMBER, "Nr obwodu: _ _ _ _");
        if(!periphery_number.equalsIgnoreCase("Nr obwodu: _ _ _ _"))
            periphery_number = "Nr obwodu: "+periphery_number;
        String periphery_name = sharedPref.getString(Utils.PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(Utils.PERIPHERY_ADDRESS, "Adres");
        String districtNumber = sharedPref.getString(Utils.DISTRICT_NUMBER, "Okręg Wyborczy Nr");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN),"Kod terytorialny: ".length(), territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        spinnerData.add(getString(R.string.committee_label));
        spinnerData.add(periphery_name);
        spinnerData.add(periphery_address);
        spinnerData.add(districtNumber);
    }

    View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent nextPhotoIntent = new Intent(getActivity(), NextPhotoActivity.class);
            nextPhotoIntent.putExtra(Utils.PATH_TO_PHOTO, mCurrentPhotoPath);
            startActivity(nextPhotoIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener retryButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //delete incorrect photo
            if(mCurrentPhotoPath!=null){
                File file  = new File(mCurrentPhotoPath);
                boolean isDeleted = file.delete();
            }

            Intent cameraIntent = new Intent(getActivity(), CameraActivity.class);
            startActivity(cameraIntent);
            getActivity().finish();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            loadImage(mCurrentPhotoPath);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null)
        {
            mCurrentPhotoPath = savedInstanceState.getString(Utils.PATH_TO_PHOTO, null);
        }
    }

    public void onResume(){
        super.onResume();

        mCurrentPhotoPath = getArguments().getString(Utils.PATH_TO_PHOTO, null);
        if(mCurrentPhotoPath!=null)
        {
            loadImage(mCurrentPhotoPath);
        }
    }

    private void loadImage(String path) {
        // Get the dimensions of the View
        photoImageView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int targetW = photoImageView.getMeasuredWidth();
        int targetH  = photoImageView.getMeasuredHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        int rotate = 0 ;
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case 0:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        photoImageView.setRotation(rotate);
        photoImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Utils.PATH_TO_PHOTO,mCurrentPhotoPath);
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
