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
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.ThumbnailsActivity;
import pl.openpkw.openpkwmobile.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NextPhotoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NextPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NextPhotoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;

    private ImageView photoImageView;

    private Button nextButton;

    private String mCurrentPhotoPath;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public NextPhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NextPhotoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NextPhotoFragment newInstance(String param1, String param2) {
        NextPhotoFragment fragment = new NextPhotoFragment();
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
        View nextPhotoView = inflater.inflate(R.layout.fragment_next_photo, container, false);
        photoImageView = (ImageView) nextPhotoView.findViewById(R.id.next_photo_image_view_photo);

        Button retryButton = (Button) nextPhotoView.findViewById(R.id.next_photo_retry_button);
        retryButton.setOnClickListener(retryButtonClickListener);

        nextButton = (Button) nextPhotoView.findViewById(R.id.next_photo_next_button);
        nextButton.setOnClickListener(nextButtonClickListener);

        territorialCodeTextView = (TextView) nextPhotoView.findViewById(R.id.next_photo_territorial_code);
        peripheryNumberTextView = (TextView)nextPhotoView.findViewById(R.id.next_photo_periphery_number);

        loadData();

        return nextPhotoView;
    }

    public void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String territorial_code = sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny");
        String periphery_number = "Nr "+sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0 ,territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText( spannable);
        peripheryNumberTextView.setText(periphery_number);
    }

    View.OnClickListener retryButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dispatchTakePictureIntent();
        }
    };

    View.OnClickListener nextButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getCommitteeProtocolStorageDir(Utils.STORAGE_PROTOCOL_DIRECTORY).listFiles().length>=Utils.PHOTO_NUMBER)
            {
                Intent thumbnailsIntent = new Intent(getActivity(), ThumbnailsActivity.class);
                startActivity(thumbnailsIntent);
                getActivity().finish();

            }else
                dispatchTakePictureIntent();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if(getCommitteeProtocolStorageDir(Utils.STORAGE_PROTOCOL_DIRECTORY).listFiles().length>=Utils.PHOTO_NUMBER)
            {
                nextButton.setText("Koniec");
            }
            loadImage(mCurrentPhotoPath);
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
        String imageFileName = "CP_" + timeStamp;
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
