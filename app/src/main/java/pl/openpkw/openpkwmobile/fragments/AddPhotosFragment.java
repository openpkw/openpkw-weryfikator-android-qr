package pl.openpkw.openpkwmobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.NextPhotoActivity;
import pl.openpkw.openpkwmobile.activities.QueryAddPhotosActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.utils.Utils.PERMISSION_REQUEST_CAMERA;
import static pl.openpkw.openpkwmobile.utils.Utils.PERMISSION_WRITE_EXTERNAL_STORAGE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPhotosFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    private ContextThemeWrapper contextThemeWrapper;

    private List<String> spinnerData = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AddPhotosFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddPhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddPhotosFragment newInstance(String param1, String param2) {
        AddPhotosFragment fragment = new AddPhotosFragment();
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
        View addPhotosView = inflater.inflate(R.layout.fragment_add_photos, container, false);

        Button forwardButton = (Button) addPhotosView.findViewById(R.id.add_photos_forward_button);
        forwardButton.setOnClickListener(forwardButtonClickListener);

        Button takePhotoImageButton = (Button) addPhotosView.findViewById(R.id.add_photos_take_photo_button);
        takePhotoImageButton.setOnClickListener(takePhotoImageButtonClickListener);

        territorialCodeTextView = (TextView) addPhotosView .findViewById(R.id.add_photos_territorial_code);
        peripheryNumberTextView = (TextView) addPhotosView .findViewById(R.id.add_photos_periphery_number);

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        loadData();

        Spinner protocolDataSpinner = (Spinner) addPhotosView.findViewById(R.id.add_photos_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);

        return addPhotosView;
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
        String territorial_code = "  "+sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny")+"  ";
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        String periphery_name = sharedPref.getString(Utils.PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(Utils.PERIPHERY_ADDRESS, "Adres");
        String districtNumber = sharedPref.getString(Utils.DISTRICT_NUMBER, "Okręg Wyborczy Nr");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new BackgroundColorSpan(Color.GREEN),0, territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        spinnerData.add(getString(R.string.committee_label));
        spinnerData.add(periphery_name);
        spinnerData.add(periphery_address);
        spinnerData.add(districtNumber);
    }

    View.OnClickListener takePhotoImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final int permissionCamera = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);

            final int permissionWriteStorage =  ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionWriteStorage == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();

            }else{
                final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                builder.setMessage("Aplikacja nie ma uprawnień dostępu do aparatu lub/i pamięci urządzenia.")
                        .setTitle(R.string.dialog_warning_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.zxing_button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Request the camera permission
                                if(permissionCamera != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.CAMERA},
                                            PERMISSION_REQUEST_CAMERA);
                                }

                                if(permissionWriteStorage  != PackageManager.PERMISSION_GRANTED)
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            PERMISSION_WRITE_EXTERNAL_STORAGE);
                            }
                        });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    };

    View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent queryAddPhotosIntent = new Intent(getActivity(), QueryAddPhotosActivity.class);
            startActivity(queryAddPhotosIntent);
            getActivity().finish();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Intent nextPhotoIntent = new Intent(getActivity(), NextPhotoActivity.class);
            nextPhotoIntent.putExtra(Utils.PATH_TO_PHOTO, mCurrentPhotoPath);
            startActivity(nextPhotoIntent);
            getActivity().finish();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String imageFileName = "CP_" + timeStamp + "_";
        //File storageDir = Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_PICTURES);
        File storageDir = getCommitteeProtocolStorageDir(Utils.STORAGE_PROTOCOL_DIRECTORY);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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
