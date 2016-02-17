package pl.openpkw.openpkwmobile.security;

/**
 * Created by Admin on 02.02.16.
 */
import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import pl.openpkw.openpkwmobile.utils.StringUtils;

public class KeyWrapper {
    private  Cipher mCipher;
    private  KeyPair mPair;
    /**
     * Create a wrapper using the public/private key pair with the given alias.
     * If no pair with that alias exists, it will be generated.
     */
    public KeyWrapper(Context context, String alias)
    {
        try {
            mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","AndroidOpenSSL");
            final KeyStore keyStore = KeyStore.getInstance(StringUtils.ANDROID_KEY_STORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(alias)) {
                generateKeyPair(context, alias);
            }
            // Even if we just generated the key, always read it back to ensure we
            // can read it successfully.
            final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    alias, null);
            mPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void generateKeyPair(Context context, String alias)
            throws GeneralSecurityException {
        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);
        final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        final KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        gen.initialize(spec, new SecureRandom());
        gen.generateKeyPair();
    }

    public byte[] wrapPrivateKey(PrivateKey key) {
        try {
            mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
            return mCipher.wrap(key);
        } catch (InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PrivateKey unwrapPrivateKey(byte[] blob) throws GeneralSecurityException {
        mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());
        return (PrivateKey) mCipher.unwrap(blob, "ECDSA", Cipher.PRIVATE_KEY);
    }

    public byte[] wrapPublicKey(PublicKey key) throws GeneralSecurityException {
        mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
        return mCipher.wrap(key);
    }

    public PublicKey unwrapPublicKey(byte[] blob) throws GeneralSecurityException {
        mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());
        return (PublicKey) mCipher.unwrap(blob, "ECDSA", Cipher.PUBLIC_KEY);
    }

    public byte[] wrapOAuthID(String key) {
        try {
            mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
            return mCipher.wrap(convertStringToKey(key));
        } catch (InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Key unwrapOAuthID(byte[] blob){
        try {
            mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());
            return mCipher.unwrap(blob, "AES", Cipher.SECRET_KEY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Key convertStringToKey(String key) {
        byte[] base64Arr = Base64.decode(key, Base64.DEFAULT);
        return new SecretKeySpec(base64Arr, 0, base64Arr.length, "AES");
    }
}
