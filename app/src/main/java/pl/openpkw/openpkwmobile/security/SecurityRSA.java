package pl.openpkw.openpkwmobile.security;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.security.auth.x500.X500Principal;

import pl.openpkw.openpkwmobile.utils.StringUtils;

/**
 * Created by Admin on 17.02.16.
 */
public class SecurityRSA {

    public static void generateKeyPair(Context context, String alias)
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
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(StringUtils.RSA, StringUtils.ANDROID_KEY_STORE);
        keyGen.initialize(spec, new SecureRandom());
        keyGen.generateKeyPair();
    }

    public static PublicKey loadPublicKey(String keyAlias) {
        java.security.KeyStore keyStore;
        try {
            keyStore = java.security.KeyStore.getInstance(StringUtils.ANDROID_KEY_STORE);
            keyStore.load(null);
            java.security.KeyStore.Entry keyEntry = keyStore.getEntry(keyAlias, null);
            return ((java.security.KeyStore.PrivateKeyEntry) keyEntry)
                    .getCertificate().getPublicKey();
        } catch (KeyStoreException | CertificateException
                | UnrecoverableEntryException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey loadPrivateKey(String keyAlias) {
        java.security.KeyStore keyStore;
        try {
            keyStore = java.security.KeyStore.getInstance(StringUtils.ANDROID_KEY_STORE);
            keyStore.load(null);
            java.security.KeyStore.Entry keyEntry = keyStore.getEntry(keyAlias, null);
            return ((java.security.KeyStore.PrivateKeyEntry) keyEntry)
                    .getPrivateKey();
        } catch (KeyStoreException | CertificateException
                | NoSuchAlgorithmException | UnrecoverableEntryException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object
            final Cipher cipher = Cipher.getInstance(StringUtils.ENCRYPTION_MODE_RSA, StringUtils.PROVIDER_OPEN_SSL);
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static String decrypt(byte [] encryptedText, PrivateKey key) {
        try {
            Cipher output = Cipher.getInstance(StringUtils.ENCRYPTION_MODE_RSA, StringUtils.PROVIDER_OPEN_SSL);
            output.init(Cipher.DECRYPT_MODE, key);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(encryptedText), output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }
            return  new String(bytes, 0, bytes.length, StringUtils.CHARACTER_ENCODING);

        } catch (Exception e) {
            return null;
        }
    }
}
