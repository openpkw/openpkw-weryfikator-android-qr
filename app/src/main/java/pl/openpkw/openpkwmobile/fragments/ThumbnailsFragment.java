package pl.openpkw.openpkwmobile.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.activities.EndActivity;
import pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity;
import pl.openpkw.openpkwmobile.models.OAuthParam;
import pl.openpkw.openpkwmobile.models.QrDTO;
import pl.openpkw.openpkwmobile.network.GetAccessToken;
import pl.openpkw.openpkwmobile.network.NetworkUtils;
import pl.openpkw.openpkwmobile.network.QrSendResponse;
import pl.openpkw.openpkwmobile.network.SendQrData;
import pl.openpkw.openpkwmobile.security.SecurityRSA;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;
import static pl.openpkw.openpkwmobile.utils.Utils.ACCESS_TOKEN;
import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.DEFAULT_PARAM_CHANGE;
import static pl.openpkw.openpkwmobile.utils.Utils.DIALOG_STYLE;
import static pl.openpkw.openpkwmobile.utils.Utils.DISTRICT_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.ERROR;
import static pl.openpkw.openpkwmobile.utils.Utils.ERROR_MESSAGE;
import static pl.openpkw.openpkwmobile.utils.Utils.ID_DEFAULT;
import static pl.openpkw.openpkwmobile.utils.Utils.IS_DATA_SEND;
import static pl.openpkw.openpkwmobile.utils.Utils.KEY_ALIAS;
import static pl.openpkw.openpkwmobile.utils.Utils.OAUTH2_ID_PREFERENCE;
import static pl.openpkw.openpkwmobile.utils.Utils.OAUTH2_SECRET_PREFERENCE;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_ADDRESS;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.QR;
import static pl.openpkw.openpkwmobile.utils.Utils.REFRESH_TOKEN;
import static pl.openpkw.openpkwmobile.utils.Utils.SECRET_DEFAULT;
import static pl.openpkw.openpkwmobile.utils.Utils.SERVER_RESPONSE;
import static pl.openpkw.openpkwmobile.utils.Utils.STORAGE_PROTOCOL_DIRECTORY;
import static pl.openpkw.openpkwmobile.utils.Utils.TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;
import static pl.openpkw.openpkwmobile.utils.Utils.URL_DEFAULT_LOGIN;
import static pl.openpkw.openpkwmobile.utils.Utils.URL_DEFAULT__VERIFY_QR;
import static pl.openpkw.openpkwmobile.utils.Utils.URL_LOGIN_PREFERENCE;
import static pl.openpkw.openpkwmobile.utils.Utils.URL_VERIFY_PREFERENCE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ThumbnailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ThumbnailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThumbnailsFragment extends Fragment implements View.OnClickListener{

    private TableLayout thumbnailsTableLayout;
    private File[] photoFiles;
    private String qrString;
    private OAuthParam oAuthParam;
    private QrDTO scanQrDTO = null;

    private TextView territorialCodeTextView;
    private TextView peripheryNumberTextView;

    private List<String> spinnerData = new ArrayList<>();

    private ContextThemeWrapper contextThemeWrapper;

    private OnFragmentInteractionListener mListener;

    private Handler mHandler;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    final int id = 1;

    public ThumbnailsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ThumbnailsFragment newInstance() {
        return new ThumbnailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thumbnailsView = inflater.inflate(R.layout.fragment_thumbnails, container, false);

        Button sendPictureButton = (Button) thumbnailsView.findViewById(R.id.thumbnails_send_button);
        sendPictureButton.setOnClickListener(this);

        Button wifiSendPictureButton = (Button) thumbnailsView.findViewById(R.id.thumbnails_send_wifi_button);
        wifiSendPictureButton.setOnClickListener(this);

        thumbnailsTableLayout = (TableLayout) thumbnailsView.findViewById(R.id.thumbnails_photos_layout);

        photoFiles = getCommitteeProtocolStorageDir(STORAGE_PROTOCOL_DIRECTORY).listFiles();

        contextThemeWrapper = new ContextThemeWrapper(getActivity(), DIALOG_STYLE);

        territorialCodeTextView = (TextView) thumbnailsView.findViewById(R.id.thumbnails_territorial_code);
        peripheryNumberTextView = (TextView) thumbnailsView.findViewById(R.id.thumbnails_periphery_number);

        loadData();

        loadThumbnails();

        Spinner protocolDataSpinner = (Spinner) thumbnailsView.findViewById(R.id.thumbnails_data_spinner);
        //set data adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.view_spinner_item, spinnerData);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolDataSpinner.setAdapter(adapter);
        protocolDataSpinner.setOnItemSelectedListener(spinnerItemListener);

        mHandler = new Handler();
        mNotifyManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getActivity());
        mBuilder.setContentTitle("OpenPKW")
                .setContentText("Przesyłanie zdjęć w toku")
                .setSmallIcon(android.R.drawable.stat_sys_upload);

        return thumbnailsView;
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

    private void loadData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
        String territorial_code = sharedPref.getString(TERRITORIAL_CODE, "Kod terytorialny: _ _ _ _");
        if(!territorial_code.equalsIgnoreCase("Kod terytorialny: _ _ _ _"))
            territorial_code = "Kod terytorialny: "+territorial_code;
        String periphery_number = sharedPref.getString(PERIPHERY_NUMBER, "Nr obwodu: _ _ _ _");
        if(!periphery_number.equalsIgnoreCase("Nr obwodu: _ _ _ _"))
            periphery_number = "Nr obwodu: "+periphery_number;
        String periphery_name = sharedPref.getString(PERIPHERY_NAME, "Nazwa");
        String periphery_address = sharedPref.getString(PERIPHERY_ADDRESS, "Adres");
        String districtNumber = sharedPref.getString(DISTRICT_NUMBER, "Okręg Wyborczy Nr");
        Spannable spannable = new SpannableString(territorial_code);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN),"Kod terytorialny: ".length(), territorial_code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        territorialCodeTextView.setText(spannable);
        peripheryNumberTextView.setText(periphery_number);
        spinnerData.add(getString(R.string.committee_label));
        spinnerData.add(periphery_name);
        spinnerData.add(periphery_address);
        spinnerData.add(districtNumber);
    }

    private OAuthParam getOAuthParam()
    {
        String [] urls = getUrls();
        OAuthParam oAuthParam = new OAuthParam();
        SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
        oAuthParam.setLoginURL(urls[0]);
        oAuthParam.setSendQrURL(urls[1]);
        String decryptToken = SecurityRSA.decrypt(Base64.decode(sharedPref.getString(REFRESH_TOKEN, null)
                , Base64.DEFAULT), SecurityRSA.loadPrivateKey(KEY_ALIAS));
        oAuthParam.setRefreshToken(decryptToken);
        qrString = sharedPref.getString(QR, null);

        if(sharedPref.getBoolean(DEFAULT_PARAM_CHANGE,false)){
            PrivateKey privateKey = SecurityRSA.loadPrivateKey(KEY_ALIAS);
            String id = sharedPref.getString(OAUTH2_ID_PREFERENCE, ID_DEFAULT).trim();
            String decryptID = SecurityRSA.decrypt(Base64.decode(id, Base64.DEFAULT), privateKey);
            oAuthParam.setId(decryptID);
            String secret = sharedPref.getString(OAUTH2_SECRET_PREFERENCE, SECRET_DEFAULT).trim();
            String decryptSecret = SecurityRSA.decrypt(Base64.decode(secret, Base64.DEFAULT), privateKey);
            oAuthParam.setSecret(decryptSecret);
        }
        else {
            oAuthParam.setId(sharedPref.getString(OAUTH2_ID_PREFERENCE, ID_DEFAULT).trim());
            oAuthParam.setSecret(sharedPref.getString(OAUTH2_SECRET_PREFERENCE, SECRET_DEFAULT).trim());
        }
        return oAuthParam;
    }

    private String [] getUrls(){
        String [] urls = new String[2];
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        urls[0]= sharedPref.getString(URL_LOGIN_PREFERENCE, URL_DEFAULT_LOGIN).trim();
        urls[1]= sharedPref.getString(URL_VERIFY_PREFERENCE, URL_DEFAULT__VERIFY_QR).trim();
        return urls;
    }

    private void clearQRSharedPreferences(String keyQR, SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyQR, null);
        editor.apply();
    }

    private void loadThumbnails() {
        //load committee protocol photo files
        photoFiles = getCommitteeProtocolStorageDir(STORAGE_PROTOCOL_DIRECTORY).listFiles();
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

    public static File getCommitteeProtocolStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "DIRECTORY NOT CREATED");
        }
        return file;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonSendByWifi() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
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
        switch (view.getId()) {
            case R.id.thumbnails_send_button: {
                oAuthParam = getOAuthParam();
                final AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                if(qrString!=null)
                {
                    if(NetworkUtils.isNetworkAvailable(getActivity())) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int incr;
                                for (incr = 0; incr <= 100; incr+=5) {
                                    // Sets the progress indicator to a max value, the
                                    // current completion percentage, and "determinate"
                                    // state
                                    mBuilder.setProgress(100, incr, false);
                                    // Displays the progress bar for the first time.
                                    mNotifyManager.notify(id, mBuilder.build());
                                    // Sleeps the thread, simulating an operation
                                    // that takes time
                                    try {
                                        // Sleep for 2.5 seconds
                                        Thread.sleep(5*50);
                                    } catch (InterruptedException e) {
                                        Log.d(Utils.TAG, "sleep failure");
                                    }
                                }
                                // When the loop is finished, updates the notification
                                mBuilder.setContentText("Zdjęcia zostały przesłane na serwer")
                                        // Removes the progress bar
                                        .setProgress(0,0,false);
                                mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                                mNotifyManager.notify(id, mBuilder.build());
                                //delete picture
                                deletePictures();
                                //start end activity
                                Intent endIntent = new Intent(getActivity(), EndActivity.class);
                                startActivity(endIntent);
                                getActivity().finish();
                            }
                        });
                        /*
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
                        KeyWrapper keyWrapper = new KeyWrapper(getActivity().getApplicationContext(), KEY_ALIAS);
                        String privateKeyStr = sharedPref.getString(PRIVATE_KEY,null);
                        PrivateKey privateKey  = null;
                        try {
                            privateKey = keyWrapper.unwrapPrivateKey(Base64.decode(privateKeyStr,Base64.DEFAULT));
                        } catch (GeneralSecurityException | IOException e) {
                            e.printStackTrace();
                        }

                        scanQrDTO = new QrDTO();
                        scanQrDTO.setQr(qrString);
                        scanQrDTO.setToken(Base64.encodeToString(SecurityECC.generateSignature(qrString, privateKey), Base64.NO_WRAP));

                        GetAccessTokenAsyncTask getAccessTokenAsyncTask = new GetAccessTokenAsyncTask();
                        getAccessTokenAsyncTask.execute(oAuthParam);
                        */

                    }else
                    {
                        builder.setMessage(R.string.login_toast_no_network_connection_message)
                                .setTitle(R.string.login_toast_no_network_connection_title)
                                .setCancelable(false)
                                .setPositiveButton(R.string.zxing_button_ok, null);
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
                else
                {
                    builder.setMessage("Proszę zeskanować kod QR z ostaniej strony protokołu wyborczego")
                            .setTitle(R.string.dialog_warning_title)
                            .setCancelable(false)
                            .setPositiveButton(R.string.zxing_button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //start scan activity
                                    Intent scanIntent = new Intent(getActivity(), ScanQrCodeActivity.class);
                                    startActivity(scanIntent);
                                    getActivity().finish();
                                }
                            });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            }

            case R.id.thumbnails_send_wifi_button:{
                //
                SharedPreferences sharedPref = getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(IS_DATA_SEND, false);
                editor.apply();

                Toast toast = Toast.makeText(getActivity(), R.string.toast_send_picture_by_wifi, Toast.LENGTH_LONG);
                View viewToast = toast.getView();
                viewToast.setBackgroundResource(R.drawable.toast_green);
                TextView text = (TextView) viewToast.findViewById(android.R.id.message);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                    text.setTextColor(getResources().getColor(android.R.color.white,getActivity().getTheme()));
                else
                    text.setTextColor(getResources().getColor(android.R.color.white));
                text.setTypeface(Typeface.DEFAULT_BOLD);
                text.setGravity(Gravity.CENTER);
                toast.show();

                //cancel timer
                if(timer!=null)
                    timer.cancel();

                Intent endIntent = new Intent(getActivity(), EndActivity.class);
                startActivity(endIntent);
                getActivity().finish();

                break;
            }

        }
    }

    private void deletePictures() {
        if(photoFiles == null || photoFiles.length==0){
            photoFiles = getCommitteeProtocolStorageDir(STORAGE_PROTOCOL_DIRECTORY).listFiles();
        }
        for (File photoFile : photoFiles) {
            photoFile.delete();
        }
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
        void onFragmentInteraction();
    }

    class ThumbnailsLoader extends AsyncTask<File, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String absoluteFilePath;
        private Bitmap imageBitmap;

        public ThumbnailsLoader(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(File... files) {
            final int THUMBNAIL_SIZE = 512;
            absoluteFilePath = files[0].getAbsolutePath();
            Log.e(TAG,"LOAD THUMBNAIL PATH: "+absoluteFilePath);
            imageBitmap = BitmapFactory.decodeFile(absoluteFilePath);
            Float width = (float) imageBitmap.getWidth();
            Float height = (float) imageBitmap.getHeight();
            Float ratio = width/height;
            return ThumbnailUtils.extractThumbnail(imageBitmap,Math.round(THUMBNAIL_SIZE*ratio), THUMBNAIL_SIZE);
        }

        @Override
        protected void onPostExecute(Bitmap thumbnails) {
            if(!imageBitmap.isRecycled())
                imageBitmap.recycle();

            if (thumbnails != null) {
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
                            case 0:
                                rotate = 90;
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
                Log.e(TAG, "SERVER RESPONSE ACCESS TOKEN: "+json.toString());
                try {

                    if(!json.getString(ERROR).isEmpty())
                    {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Błąd dostępu do serwera", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        if(!json.getString(ACCESS_TOKEN).isEmpty()) {
                            String access_token = json.getString(ACCESS_TOKEN);

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
                Log.e(TAG,"JSON SERVER RESPONSE QR: "+json.toString());
                try {
                    if(!json.getString(ERROR_MESSAGE).isEmpty()) {
                        Gson gson = new GsonBuilder().create();
                        QrSendResponse qrSendResponse = gson.fromJson(json.toString(),QrSendResponse.class);
                        Log.e(TAG,"ERROR MESSAGE: "+ qrSendResponse.getErrorMessage());
                        Log.e(TAG,"PROTOCOL: "+ qrSendResponse.getProtocol());

                        Toast.makeText(getActivity().getApplicationContext(),
                                SERVER_RESPONSE + json.toString(), Toast.LENGTH_LONG).show();

                        //clear QR data
                        clearQRSharedPreferences(QR, getActivity().getSharedPreferences(DATA, Context.MODE_PRIVATE));
                        //cancel timer
                        if(timer!=null)
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
