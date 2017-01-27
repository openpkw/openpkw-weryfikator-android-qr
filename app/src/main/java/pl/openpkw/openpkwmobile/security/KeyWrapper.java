package pl.openpkw.openpkwmobile.security;

/**
 * Created by Admin on 02.02.16.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.utils.Utils.ENCRYPTION_MODE_RSA;

public class KeyWrapper {
    private Cipher cipher;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    /**
     * Create a wrapper using the public/private key pair with the given alias.
     * If no pair with that alias exists, it will be generated.
     */
    @SuppressLint("GetInstance")
    public KeyWrapper(Context context, String alias)
    {
        try {
            cipher = Cipher.getInstance(ENCRYPTION_MODE_RSA);
            final KeyStore keyStore = KeyStore.getInstance(Utils.ANDROID_KEY_STORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(alias)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                    SecurityRSA.generateKeyPair(alias);
                else
                    SecurityRSA.generateKeyPair(context, alias);

            }
            privateKey = (PrivateKey) keyStore.getKey(alias, null);
            publicKey = keyStore.getCertificate(alias).getPublicKey();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized byte[] wrapPublicKey(PublicKey key) throws GeneralSecurityException, IOException {
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }

    public synchronized PublicKey unwrapPublicKey(byte[] blob) throws GeneralSecurityException, IOException {
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        return (PublicKey) cipher.unwrap(blob, "ECDSA", Cipher.PUBLIC_KEY);
    }

    public synchronized byte[] wrapPrivateKey(PrivateKey key) throws GeneralSecurityException, IOException {
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }

    public synchronized PrivateKey unwrapPrivateKey(byte[] blob) throws GeneralSecurityException, IOException {
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        return (PrivateKey) cipher.unwrap(blob, "ECDSA", Cipher.PRIVATE_KEY);
    }
}
