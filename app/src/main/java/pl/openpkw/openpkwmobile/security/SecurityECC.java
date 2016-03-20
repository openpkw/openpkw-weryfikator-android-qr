package pl.openpkw.openpkwmobile.security;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.jce.ECNamedCurveTable;

import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.IESParameterSpec;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import pl.openpkw.openpkwmobile.utils.StringUtils;


public class SecurityECC {

    //generate ECDSA keys
    public static KeyPair generateKeys()
    {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(StringUtils.CURVE);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(StringUtils.ECDSA, StringUtils.SECURITY_PROVIDER);
            keyPairGenerator.initialize(ecSpec, new SecureRandom());
            return keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("ResourceType")
    public static void generateKeysWithAndroidKeyStore(Context context, String alias)
            throws GeneralSecurityException {
        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("P-256");
        final KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.ONE)
                .setKeyType("EC")
                .setAlgorithmParameterSpec(ecGenParameterSpec)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(StringUtils.RSA, StringUtils.ANDROID_KEY_STORE);
        keyGen.initialize(spec, new SecureRandom());
        keyGen.generateKeyPair();
    }

    //generate ECDSA signature
    public static byte [] generateSignature(String data,PrivateKey privateKey)
    {
        try {
            Signature signature = Signature.getInstance(StringUtils.SIGNATURE_INSTANCE, StringUtils.SECURITY_PROVIDER);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StringUtils.CHARACTER_ENCODING));
            return signature.sign();

        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | UnsupportedEncodingException | NoSuchProviderException e) {
            Log.e(StringUtils.TAG,"ECDSA SIGNATURE ERROR: "+e.getMessage());
        }
        return null;
    }

    //verify ECDSA signature
    public static boolean signatureVerification (PublicKey publicKey, String data, byte [] signature)
    {
        try {
            Signature signVerify = Signature.getInstance(StringUtils.SIGNATURE_INSTANCE, StringUtils.SECURITY_PROVIDER);
            signVerify.initVerify(publicKey);
            signVerify.update(data.getBytes(StringUtils.CHARACTER_ENCODING));
            return signVerify.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException |
                InvalidKeyException | UnsupportedEncodingException | NoSuchProviderException e) {
            Log.e(StringUtils.TAG,"ECDSA VERIFICATION SIGNATURE ERROR: "+e.getMessage());
        }
        return false;
    }

    public static PublicKey getPublicKeyFromBase64(String publicKey){
        try{
            byte[] bytePublicKey = Base64.decode(publicKey.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(bytePublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(StringUtils.ECDSA, StringUtils.SECURITY_PROVIDER);
            return keyFactory.generatePublic(X509publicKey);
        }
        catch(Exception e){
            Log.e(StringUtils.TAG,"ERROR CREATE PUBLIC KEY: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKeyFromBase64(String privateKey){
        try{
            byte[] bytePrivateKey = Base64.decode(privateKey.getBytes(), Base64.DEFAULT);
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
            KeyFactory keyFactory = KeyFactory.getInstance(StringUtils.ECDSA, StringUtils.SECURITY_PROVIDER);
            return keyFactory.generatePrivate(encodedKeySpec);
        }
        catch(Exception e){
            Log.e(StringUtils.TAG,"ERROR CREATE PRIVATE KEY: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //data encryption - ECIES
    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an ECIES cipher object
            final Cipher cipher = Cipher.getInstance(StringUtils.ECIES, StringUtils.SECURITY_PROVIDER);
            // encrypt the plain text using the ECDSA public key
            cipher.init(Cipher.ENCRYPT_MODE, key, new SecureRandom());
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            Log.e(StringUtils.TAG, "Encryption error: "+e.getMessage());
        }
        return cipherText;
    }

    //data decryption - ECIES
    public static byte[] decrypt(byte [] encryptText, PrivateKey key) {
        byte[] cipherText = null;
        try {
            // get an ECIES cipher object
            final Cipher cipher = Cipher.getInstance(StringUtils.ECIES, StringUtils.SECURITY_PROVIDER);
            // decrypt the text using the ECDSA private key
            cipher.init(Cipher.DECRYPT_MODE, key, new SecureRandom());
            cipherText = cipher.doFinal(encryptText);
        } catch (Exception e) {
            Log.e(StringUtils.TAG, "Decryption error: "+e.getMessage());
        }
        return cipherText;
    }

    public static String signEc(PrivateKey privateKey, String toSign) {
        try {
            Signature sig = Signature.getInstance("SHA512WITHECDSA");
            sig.initSign(privateKey);
            byte[] signedData = toSign.getBytes("UTF-8");
            sig.update(signedData);
            byte[] signature = sig.sign();

            return toBase64(signature);
        } catch (GeneralSecurityException | IOException | DataLengthException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyEc(String signatureStr, String signedStr,
                                   PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA512WITHECDSA");
            sig.initVerify(publicKey);
            byte[] signedData = signedStr.getBytes("UTF-8");
            byte[] signature = fromBase64(signatureStr);
            sig.update(signedData);
            return sig.verify(signature);

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    public static PublicKey loadPublicKey(String keyAlias)
            throws GeneralSecurityException, IOException {
        java.security.KeyStore ks = java.security.KeyStore
                .getInstance("AndroidKeyStore");
        ks.load(null);
        java.security.KeyStore.Entry keyEntry = ks.getEntry(keyAlias, null);

        return ((java.security.KeyStore.PrivateKeyEntry) keyEntry)
                .getCertificate().getPublicKey();
    }

    public static PrivateKey loadPrivateKey(String keyAlias)
            throws GeneralSecurityException, IOException {
        java.security.KeyStore ks = java.security.KeyStore
                .getInstance("AndroidKeyStore");
        ks.load(null);
        java.security.KeyStore.Entry keyEntry = ks.getEntry(keyAlias, null);

        return ((java.security.KeyStore.PrivateKeyEntry) keyEntry)
                .getPrivateKey();
    }

}
