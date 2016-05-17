package pl.openpkw.openpkwmobile.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.EndActivity;
import pl.openpkw.openpkwmobile.models.OAuthParam;
import pl.openpkw.openpkwmobile.models.QrDTO;
import pl.openpkw.openpkwmobile.network.GetAccessToken;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.network.QrSendResponse;
import pl.openpkw.openpkwmobile.network.SendQrData;
import pl.openpkw.openpkwmobile.security.KeyWrapper;
import pl.openpkw.openpkwmobile.security.SecurityECC;
import pl.openpkw.openpkwmobile.security.SecurityRSA;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ThumbnailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ThumbnailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThumbnailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TableLayout thumbnailsTableLayout;

    private File[] photoFiles;

    private String qrString;
    private OAuthParam oAuthParam;
    private QrDTO scanQrDTO = null;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    private ContextThemeWrapper contextThemeWrapper;

    private OnFragmentInteractionListener mListener;

    public ThumbnailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ThumbnailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ThumbnailsFragment newInstance(String param1, String param2) {
        ThumbnailsFragment fragment = new ThumbnailsFragment();
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
        View thumbnailsView = inflater.inflate(R.layout.fragment_thumbnails, container, false);

        Button sendPhotos = (Button) thumbnailsView.findViewById(R.id.thumbnails_send_button);
        sendPhotos.setOnClickListener(sendPhotosClickListener);

        thumbnailsTableLayout = (TableLayout) thumbnailsView.findViewById(R.id.thumbnails_photos_layout);

        photoFiles = getCommitteeProtocolStorageDir(Utils.STORAGE_PROTOCOL_DIRECTORY).listFiles();

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), Utils.DIALOG_STYLE);

        territorialCodeTextView = (TextView) thumbnailsView.findViewById(R.id.thumbnails_territorial_code);
        peripheryNumberTextView = (TextView) thumbnailsView.findViewById(R.id.thumbnails_periphery_number);

        loadData();

        loadThumbnails();

        return thumbnailsView;
    }

    public void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        String territorial_code = sharedPref.getString(Utils.TERRITORIAL_CODE, "Kod terytorialny");
        String periphery_number = "Nr " + sharedPref.getString(Utils.PERIPHERY_NUMBER, "obwodu");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
    }

    View.OnClickListener sendPhotosClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            oAuthParam = getOAuthParam();

            if(qrString!=null)
            {
                if(NetworkUtils.isNetworkAvailable(getActivity())) {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
                    KeyWrapper keyWrapper = new KeyWrapper(getActivity().getApplicationContext(), Utils.KEY_ALIAS);
                    String privateKeyStr = sharedPref.getString(Utils.PRIVATE_KEY,null);
                    PrivateKey privateKey = null;
                    try {
                        privateKey = keyWrapper.unwrapPrivateKey(Base64.decode(privateKeyStr,Base64.DEFAULT));
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                    scanQrDTO = new QrDTO();
                    scanQrDTO.setQr(qrString);
                    scanQrDTO.setToken(Base64.encodeToString(SecurityECC.generateSignature(qrString, privateKey), Base64.NO_WRAP));

                    GetAccessTokenAsyncTask getAccessTokenAsyncTask = new GetAccessTokenAsyncTask();
                    getAccessTokenAsyncTask.execute(oAuthParam);

                }else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                    builder.setMessage(R.string.login_toast_no_network_connection_message)
                            .setTitle(R.string.login_toast_no_network_connection_title)
                            .setCancelable(false)
                            .setPositiveButton(R.string.zxing_button_ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            else
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.scan_qr_toast_please_scan_qr),
                        Toast.LENGTH_LONG).show();
        }
    };


    private OAuthParam getOAuthParam()
    {
        String [] urls = getUrls();
        OAuthParam oAuthParam = new OAuthParam();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE);
        oAuthParam.setLoginURL(urls[0]);
        oAuthParam.setSendQrURL(urls[1]);
        String decryptToken = SecurityRSA.decrypt(Base64.decode(sharedPref.getString(Utils.REFRESH_TOKEN, null)
                , Base64.DEFAULT), SecurityRSA.loadPrivateKey(Utils.KEY_ALIAS));
        oAuthParam.setRefreshToken(decryptToken);
        qrString = sharedPref.getString(Utils.QR, null);

        if(sharedPref.getBoolean(Utils.DEFAULT_PARAM_CHANGE,false)){
            PrivateKey privateKey = SecurityRSA.loadPrivateKey(Utils.KEY_ALIAS);
            String id = sharedPref.getString(Utils.OAUTH2_ID_PREFERENCE, Utils.ID_DEFAULT).trim();
            String decryptID = SecurityRSA.decrypt(Base64.decode(id, Base64.DEFAULT), privateKey);
            oAuthParam.setId(decryptID);
            String secret = sharedPref.getString(Utils.OAUTH2_SECRET_PREFERENCE, Utils.SECRET_DEFAULT).trim();
            String decryptSecret = SecurityRSA.decrypt(Base64.decode(secret, Base64.DEFAULT), privateKey);
            oAuthParam.setSecret(decryptSecret);
        }
        else {
            oAuthParam.setId(sharedPref.getString(Utils.OAUTH2_ID_PREFERENCE, Utils.ID_DEFAULT).trim());
            oAuthParam.setSecret(sharedPref.getString(Utils.OAUTH2_SECRET_PREFERENCE, Utils.SECRET_DEFAULT).trim());
        }
        return oAuthParam;
    }

    private String [] getUrls(){
        String [] urls = new String[2];
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        urls[0]= sharedPref.getString(Utils.URL_LOGIN_PREFERENCE, Utils.URL_DEFAULT_LOGIN).trim();
        urls[1]= sharedPref.getString(Utils.URL_VERIFY_PREFERENCE, Utils.URL_DEFAULT__VERIFY_QR).trim();
        return urls;
    }

    private void clearQRSharedPreferences(String keyQR, SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyQR, null);
        editor.apply();
    }

    private void loadThumbnails() {
        //load committee protocol photo files
        photoFiles = getCommitteeProtocolStorageDir(Utils.STORAGE_PROTOCOL_DIRECTORY).listFiles();
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (File photoFile : photoFiles) {

            TableRow thumbnailsRow = (TableRow) layoutInflater.inflate(R.layout.thumbnails_tablerow, null);
            TableRow thumbnailsSeparatorRow = (TableRow) layoutInflater.inflate(R.layout.thumbnails_separator_tablerow, null);
            ImageView thumbnailsImageView = (ImageView) thumbnailsRow.findViewById(R.id.thumbnails_image_view);
            thumbnailsTableLayout.addView(thumbnailsSeparatorRow);
            thumbnailsTableLayout.addView(thumbnailsRow);

            ThumbnailsLoader thumbnailsLoader =new ThumbnailsLoader(thumbnailsImageView);
            thumbnailsLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,photoFile);
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

    class ThumbnailsLoader extends AsyncTask<File, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String absoluteFilePath;

        public ThumbnailsLoader(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(File... files) {
            absoluteFilePath = files[0].getAbsolutePath();
            return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(absoluteFilePath), 512, 384);
        }

        @Override
        protected void onPostExecute(Bitmap thumbnails) {
            if (imageViewReference != null && thumbnails != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    int rotate = 0 ;
                    ExifInterface exif;
                    try {
                        exif = new ExifInterface(absoluteFilePath);
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

                    imageView.setRotation(rotate);
                    imageView.setImageBitmap(thumbnails);
                }
            }
        }
    }

    private class GetAccessTokenAsyncTask extends AsyncTask<OAuthParam, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(OAuthParam... oAuthParams) {
            GetAccessToken jParser = new GetAccessToken();
            return jParser.getToken(oAuthParams[0].getLoginURL(), oAuthParams[0].getId(),
                    oAuthParams[0].getSecret(), oAuthParams[0].getRefreshToken());
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            if (json != null){
                Log.e(Utils.TAG, "SERVER RESPONSE ACCESS TOKEN: "+json.toString());
                try {

                    if(!json.getString(Utils.ERROR).isEmpty())
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Błąd dostępu do serwera", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        if(!json.getString(Utils.ACCESS_TOKEN).isEmpty()) {
                            String access_token = json.getString(Utils.ACCESS_TOKEN);

                            if (!access_token.isEmpty()) {
                                if(scanQrDTO!=null) {
                                    Gson gson = new Gson();
                                    SendQrAsyncTask sendQrAsyncTask = new SendQrAsyncTask();
                                    sendQrAsyncTask.execute(access_token, gson.toJson(scanQrDTO));
                                }
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

            }else{
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.login_toast_no_connection), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class SendQrAsyncTask extends AsyncTask<String, String, JSONObject>{

        private ProgressDialog progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //create progress bar
            progressBar = new ProgressDialog(contextThemeWrapper);
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.scan_qr_progress_send_data));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.setCancelable(false);
            progressBar.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            SendQrData jParser = new SendQrData();
            return jParser.sendQR(oAuthParam.getSendQrURL(), strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            progressBar.setProgress(100);
            progressBar.dismiss();

            if (json != null) {
                Log.e(Utils.TAG,"JSON SERVER RESPONSE QR: "+json.toString());
                try {
                    if(!json.getString(Utils.ERROR_MESSAGE).isEmpty()) {
                        Gson gson = new GsonBuilder().create();
                        QrSendResponse qrSendResponse = gson.fromJson(json.toString(),QrSendResponse.class);
                        Log.e(Utils.TAG,"ERROR MESSAGE: "+ qrSendResponse.getErrorMessage());
                        Log.e(Utils.TAG,"PROTOCOL: "+ qrSendResponse.getProtocol());

                        Toast.makeText(getActivity().getApplicationContext(),
                                Utils.SERVER_RESPONSE + json.toString(), Toast.LENGTH_LONG).show();

                        //delete photo files
                        for(File photo : photoFiles) {photo.delete();}

                        //clear QR data
                        clearQRSharedPreferences(Utils.QR, getActivity().getSharedPreferences(Utils.DATA, Context.MODE_PRIVATE));
                        //cancel timer
                        timer.cancel();
                        //start end activity
                        Intent endIntent = new Intent(getActivity(), EndActivity.class);
                        startActivity(endIntent);
                        getActivity().finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.login_toast_no_connection), Toast.LENGTH_LONG).show();
            }
        }
    }
}
