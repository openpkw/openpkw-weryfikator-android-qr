package pl.openpkw.openpkwmobile.fragments;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import pl.openpkw.openpkwmobile.activities.ThumbnailsActivity;
import pl.openpkw.openpkwmobile.camera.CameraActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NextPhotoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NextPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NextPhotoFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView photoImageView;

    private String mCurrentPhotoPath;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    private List<String> spinnerData = new ArrayList<>();

    private ContextThemeWrapper contextThemeWrapper;

    private OnFragmentInteractionListener mListener;

    public NextPhotoFragment() {
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
        View nextPhotoView = inflater.inflate(R.layout.fragment_next_photo, container, false);
        photoImageView = (ImageView) nextPhotoView.findViewById(R.id.next_photo_image_view_photo);

        Button nextPhotoButton = (Button) nextPhotoView.findViewById(R.id.next_photo_next_button);
        nextPhotoButton.setOnClickListener(nextButtonClickListener);

        Button endButton = (Button) nextPhotoView.findViewById(R.id.next_photo_end_button);
        endButton.setOnClickListener(endButtonClickListener);

        territorialCodeTextView = (TextView) nextPhotoView.findViewById(R.id.next_photo_territorial_code);
        peripheryNumberTextView = (TextView) nextPhotoView.findViewById(R.id.next_photo_periphery_number);

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        loadData();

        Spinner protocolDataSpinner = (Spinner) nextPhotoView.findViewById(R.id.next_photo_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);

        return nextPhotoView;
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
            Intent nextPhotoIntent = new Intent(getActivity(), CameraActivity.class);
            startActivity(nextPhotoIntent);
            getActivity().finish();
        }
    };

    View.OnClickListener endButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getCommitteeProtocolStorageDir(Utils.STORAGE_PROTOCOL_DIRECTORY).listFiles().length==0)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                builder.setMessage("Nie wykonano żadnego zdjęcia protokołu wyborczego.")
                        .setTitle(R.string.dialog_warning_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.zxing_button_ok, null);
                final AlertDialog dialog = builder.create();
                dialog.show();
            }else {
                Intent thumbnailsIntent = new Intent(getActivity(), ThumbnailsActivity.class);
                startActivity(thumbnailsIntent);
                getActivity().finish();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            loadImage(mCurrentPhotoPath);
        }
    }

    public File getCommitteeProtocolStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(Utils.TAG, "DIRECTORY NOT CREATED");
        }
        return file;
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
        String pathToPhoto = getArguments().getString(Utils.PATH_TO_PHOTO, null);

        if(pathToPhoto!=null)
        {
            loadImage(pathToPhoto);
            getArguments().clear();
        }
    }

    private void loadImage(String mCurrentPhotoPath) {
        Log.e(Utils.TAG,"LOAD IMAGE FROM PATH: "+mCurrentPhotoPath);
        // Get the dimensions of the View
        photoImageView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int targetW = photoImageView.getMeasuredWidth();
        int targetH  = photoImageView.getMeasuredHeight();

        Log.e(Utils.TAG,"IMAGE WIDTH TEST: "+targetW);
        Log.e(Utils.TAG,"IMAGE HEIGHT TEST: "+targetH);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int rotate = 0 ;
        ExifInterface exif;
        try {
            exif = new ExifInterface(mCurrentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e(Utils.TAG,"PHOTO ORIENTATION: "+orientation);
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
