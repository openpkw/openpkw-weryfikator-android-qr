package pl.openpkw.openpkwmobile.managers;

/**
 * Created by Kamil Gr on 05.11.15.
 */
import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

//import com.sun.org.apache.xml.internal.security.utils.Base64;

public class ServerTrustManager implements X509TrustManager{

    public ServerTrustManager() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub
        chain[0].checkValidity();
        //Log.i(TAG,"Public key: ");
        //Log.i(TAG, Base64.encode(chain[0].getPublicKey().getEncoded()));
        chain[0].getIssuerUniqueID();
        chain[0].getSubjectDN();

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // TODO Auto-generated method stub
        return null;
    }

}