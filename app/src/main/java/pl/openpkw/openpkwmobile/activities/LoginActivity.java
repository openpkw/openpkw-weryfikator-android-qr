package pl.openpkw.openpkwmobile.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.openpkw.openpkwmobile.R;
import pl.openpkw.openpkwmobile.fragments.AboutFragment;
import pl.openpkw.openpkwmobile.fragments.LoginFragment;
import pl.openpkw.openpkwmobile.fragments.SettingsFragment;
import pl.openpkw.openpkwmobile.network.Backend;
import pl.openpkw.openpkwmobile.security.KeyWrapper;
import pl.openpkw.openpkwmobile.security.SecurityECC;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.activities.ScanQrCodeActivity.showToast;
import static pl.openpkw.openpkwmobile.fragments.LoginFragment.timer;
import static pl.openpkw.openpkwmobile.utils.Utils.DATA;
import static pl.openpkw.openpkwmobile.utils.Utils.DISTRICT_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.IS_RE_LOGIN;
import static pl.openpkw.openpkwmobile.utils.Utils.KEY_ALIAS;
import static pl.openpkw.openpkwmobile.utils.Utils.LOGIN_FRAGMENT_TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_ADDRESS;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NAME;
import static pl.openpkw.openpkwmobile.utils.Utils.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.utils.Utils.PRIVATE_KEY;
import static pl.openpkw.openpkwmobile.utils.Utils.PUBLIC_KEY;
import static pl.openpkw.openpkwmobile.utils.Utils.QR;
import static pl.openpkw.openpkwmobile.utils.Utils.REQUEST_ID_MULTIPLE_PERMISSIONS;
import static pl.openpkw.openpkwmobile.utils.Utils.TAG;
import static pl.openpkw.openpkwmobile.utils.Utils.TERRITORIAL_CODE;

public class LoginActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback,
        LoginFragment.OnFragmentInteractionListener
     {

    private boolean doubleBackToExitPressedOnce = false;
         private static final String ns = null;
         private int serverID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.login_fragment_container,new LoginFragment(), LOGIN_FRAGMENT_TAG)
                    .commit();

        //set title and subtitle to action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("Krok 1 z 7");
            actionBar.setSubtitle("Logowanie do systemu");
        }

        // add spongy castle security provider
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        //generate and wrap ECDSA keys
        generateKeys();

        //check permissions
        checkAndRequestPermissions();

        //check re-login
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null) {
            if (bundle.getBoolean(IS_RE_LOGIN, false)) {
                intent.removeExtra(IS_RE_LOGIN);
            }
        }else
            clearData();

        //create config directory
        createConfigDirectory();
    }

         private void createConfigDirectory() {
             File folder = new File(Environment.getExternalStorageDirectory() + "/OpenPKW");
             boolean success = true;
             if (!folder.exists()) {
                 success = folder.mkdir();
             }
             if(success){
                 readConfigFile();
             }
         }

         private void readConfigFile() {
             File configFile = new File(Environment.getExternalStorageDirectory() + "/OpenPKW/config.txt");
             InputStream input = null;
             if(!configFile.exists()){
                 Log.e(TAG,"READ CONFIG FROM ASSETS");
                 try {
                     input = getResources().getAssets().open("config.txt");
                 } catch (IOException e) {
                     Log.e(TAG,"READ CONFIG FILE ERROR: "+e.getMessage());
                 }
             }else{
                 try {
                     input = new FileInputStream(configFile);
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 }
             }
             try {
                 List backends = parseXmlConfigFile(input);
                 if(serverID!=0) {
                     Backend defBackend;
                     for (int i = 0; i < backends.size(); i++) {
                         defBackend = (Backend) backends.get(i);
                         if (defBackend.getId() == serverID) {
                             Log.e(TAG, "BACKEND LINK " + defBackend.getLink());
                             saveServerURL(defBackend.getLink());
                         }

                     }
                 }
             } catch (XmlPullParserException | IOException e) {
                 Log.e(TAG,"PARSER ERROR:"+e.getMessage());
             }
         }

         private void saveServerURL(String link) {
             SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
             SharedPreferences.Editor editor = sharedPref.edit();
             editor.putString(Utils.URL_LOGIN_PREFERENCE, link+"/login");
             editor.putString(Utils.URL_VERIFY_PREFERENCE, link+"/qr");
             editor.putString(Utils.URL_VERIFY_PREFERENCE, link+"/users");
             editor.apply();
         }

         public List parseXmlConfigFile(InputStream in) throws XmlPullParserException, IOException {
             try {
                 XmlPullParser parser = Xml.newPullParser();
                 parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                 parser.setInput(in, null);
                 parser.nextTag();
                 return readConfig(parser);
             } finally {
                 if(in!=null)
                    in.close();
             }
         }

         private List readConfig(XmlPullParser parser) throws XmlPullParserException, IOException {
             List<Backend> backends = new ArrayList<>();
             parser.require(XmlPullParser.START_TAG, ns, "openpkw-mobile");
             parser.nextTag();
             while (parser.next() != XmlPullParser.END_TAG) {
                 if (parser.getEventType() != XmlPullParser.START_TAG) {
                     continue;
                 }
                 String name = parser.getName();

                 switch (name) {
                     case "backend":
                         backends.add(readBackend(parser));
                         break;
                     case "defaults":
                         parser.nextTag();
                         serverID = readID(parser);
                         break;
                     default:
                         skip(parser);
                         break;
                 }
             }
             return backends;
         }

         private Backend readBackend(XmlPullParser parser) throws XmlPullParserException, IOException {
             parser.require(XmlPullParser.START_TAG, ns, "backend");
             String description = null;
             String link = null;
             String name = null;
             int id = 0;
             while (parser.next() != XmlPullParser.END_TAG) {
                 if (parser.getEventType() != XmlPullParser.START_TAG) {
                     continue;
                 }
                 String nameTAG = parser.getName();
                 switch (nameTAG) {
                     case "name":
                         name = readName(parser);
                         break;
                     case "description":
                         description = readDescription(parser);
                         break;
                     case "url":
                         link = readLink(parser);
                         break;
                     case "id":
                         id = readID(parser);
                         break;
                     default:
                         skip(parser);
                         break;
                 }
             }
             return new Backend(id,description,name, link);
         }

         // Processes id tags in the backend.
         private int readID(XmlPullParser parser) throws IOException, XmlPullParserException {
             int id = 0;
             parser.require(XmlPullParser.START_TAG, ns, "id");
             String tag = parser.getName();
             if (tag.equals("id")) {
                 id = Integer.parseInt(parser.nextText());
             }
             parser.require(XmlPullParser.END_TAG, ns, "id");
             return id;
         }

         // Processes link tags in the backend.
         private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
             parser.require(XmlPullParser.START_TAG, ns, "url");
             String link= readText(parser);
             parser.require(XmlPullParser.END_TAG, ns, "url");
             return link;
         }

         // Processes description tags in the backend
         private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
             parser.require(XmlPullParser.START_TAG, ns, "description");
             String description= readText(parser);
             parser.require(XmlPullParser.END_TAG, ns, "description");
             return description;
         }

         // Processes name tags in the backend
         private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
             parser.require(XmlPullParser.START_TAG, ns, "name");
             String name = readText(parser);
             parser.require(XmlPullParser.END_TAG, ns, "name");
             return name;
         }

         private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
             String result = "";
             if (parser.next() == XmlPullParser.TEXT) {
                 result = parser.getText();
                 parser.nextTag();
             }
             return result;
         }

         private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 throw new IllegalStateException();
             }
             int depth = 1;
             while (depth != 0) {
                 switch (parser.next()) {
                     case XmlPullParser.END_TAG:
                         depth--;
                         break;
                     case XmlPullParser.START_TAG:
                         depth++;
                         break;
                 }
             }
         }

         private void clearData() {
         SharedPreferences sharedPref = getSharedPreferences(DATA, Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPref.edit();
         editor.putString(QR, null);
         editor.putString(TERRITORIAL_CODE, null);
         editor.putString(PERIPHERY_ADDRESS, null);
         editor.putString(PERIPHERY_NAME, null);
         editor.putString(PERIPHERY_NUMBER, null);
         editor.putString(DISTRICT_NUMBER, null);
         editor.apply();
     }

    private  void checkAndRequestPermissions() {
        int permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int permissionsWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionsWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                                // shouldShowRequestPermissionRationale will return true
                                //show the dialog saying its necessary and try again otherwise finish app
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showDialogOK("Dostęp do aparatu fotograficznego i pamięci urządzenia jest wymagany przez tą aplikację do prawidłowego działania. ",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            checkAndRequestPermissions();
                                                            break;
                                                        case DialogInterface.BUTTON_NEGATIVE:
                                                            finish();
                                                            break;
                                                    }
                                                }
                                            });
                                }
                            }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Anuluj", okListener)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //hide action bar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();
        //hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        //begin transaction
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Create new fragment and transaction
                Fragment settingsFragment = new SettingsFragment();
                transaction.replace(android.R.id.content, settingsFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            case R.id.about_project:
                Fragment aboutFragment = new AboutFragment();
                transaction.replace(android.R.id.content, aboutFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            //show action bar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null)
                actionBar.show();
            //show main fragment
            getFragmentManager().popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                timer.cancel();
            }

            this.doubleBackToExitPressedOnce = true;
            showToast(R.string.fragment_login_twotaptoexit,this,true);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);
        }
    }

    public void generateKeys(){
        try {
            SharedPreferences sharedPref = getSharedPreferences(DATA, Context.MODE_PRIVATE);
            KeyWrapper keyWrapper = new KeyWrapper(getApplicationContext(), KEY_ALIAS);
            if(sharedPref.getString(PRIVATE_KEY,null)==null)
            {
                KeyPair keyPair = SecurityECC.generateKeys();
                if (keyPair != null) {
                    byte [] privateKeyByteArr = keyWrapper.wrapPrivateKey(keyPair.getPrivate());
                    byte [] publicKeyByteArr = keyWrapper.wrapPublicKey(keyPair.getPublic());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(PRIVATE_KEY,Base64.encodeToString(privateKeyByteArr,Base64.DEFAULT));
                    editor.putString(PUBLIC_KEY, Base64.encodeToString(publicKeyByteArr, Base64.DEFAULT));
                    editor.apply();
                }
            }
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "GeneralSecurityException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     @Override
     public void onFragmentInteraction() {
         Intent scanIntent = new Intent(this, ScanQrCodeActivity.class);
         startActivity(scanIntent);
         finish();
     }
 }
