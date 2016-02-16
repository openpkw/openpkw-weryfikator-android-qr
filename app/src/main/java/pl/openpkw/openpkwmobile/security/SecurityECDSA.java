package pl.openpkw.openpkwmobile.security;

import android.util.Base64;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.X509Principal;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.x509.X509V3CertificateGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

import pl.openpkw.openpkwmobile.utils.StringUtils;


public class SecurityECDSA {

    private static final String SIGNATURE_INSTANCE = "SHA256withECDSA";
    private static final String ALGORITHM = "ECDSA";
    private static final String SECURITY_PROVIDER = "SC";
    private static final String CURVE = "secp256k1";
    private static final String CHARACTER_ENCODING = "UTF-8";

    public static KeyPair generateKeys()
    {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, SECURITY_PROVIDER);
            keyPairGenerator.initialize(ecSpec, new SecureRandom());
            return keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static X509Certificate generateCertificate(KeyPair keyPair){
        try {
            X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
            cert.setSerialNumber(BigInteger.valueOf(1));   //or generate a random number
            cert.setSubjectDN(new X509Principal("CN=localhost"));  //see examples to add O,OU etc
            cert.setIssuerDN(new X509Principal("CN=localhost")); //same since it is self-signed
            cert.setPublicKey(keyPair.getPublic());
            cert.setNotBefore(new Date(System.currentTimeMillis()));// time from which certificate is valid
            cert.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10)));
            cert.setSignatureAlgorithm("SHA1WithRSAEncryption");
            PrivateKey signingKey = keyPair.getPrivate();
            return cert.generate(signingKey, "SC");
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte [] generateSignature(String data,PrivateKey privateKey)
    {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_INSTANCE, SECURITY_PROVIDER);
            signature.initSign(privateKey);
            signature.update(data.getBytes(CHARACTER_ENCODING));
            return signature.sign();

        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                SignatureException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean signatureVerification (PublicKey publicKey, String data, byte [] signature)
    {
        try {
            Signature signVerify = Signature.getInstance(SIGNATURE_INSTANCE, SECURITY_PROVIDER);
            signVerify.initVerify(publicKey);
            signVerify.update(data.getBytes(CHARACTER_ENCODING));
            return signVerify.verify(signature);
        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                SignatureException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static PublicKey getPublicKeyFromBase64(String publicKey){
        try{
            byte[] bytePublicKey = Base64.decode(publicKey.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(bytePublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, SECURITY_PROVIDER);
            return keyFactory.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static PrivateKey getPrivateKeyFromBase64(String privateKey){
        try{
            byte[] bytePrivateKey = Base64.decode(privateKey.getBytes(), Base64.DEFAULT);
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, SECURITY_PROVIDER);
            return keyFactory.generatePrivate(encodedKeySpec);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
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
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding" , "AndroidOpenSSL");
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
            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding" , "AndroidOpenSSL");
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
            return  new String(bytes, 0, bytes.length, "UTF-8");

        } catch (Exception e) {
            return null;
        }
    }


}
