package pl.openpkw.openpkwmobile.security;

/**
 * Created by Admin on 02.02.16.
 */
import android.content.Context;

import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import pl.openpkw.openpkwmobile.utils.StringUtils;

public class KeyWrapper {
    private  Cipher cipher;
    private  KeyPair keyPair;
    /**
     * Create a wrapper using the public/private key pair with the given alias.
     * If no pair with that alias exists, it will be generated.
     */
    public KeyWrapper(Context context, String alias)
    {
        try {
            cipher = Cipher.getInstance(StringUtils.ENCRYPTION_MODE_RSA, StringUtils.PROVIDER_OPEN_SSL);
            final KeyStore keyStore = KeyStore.getInstance(StringUtils.ANDROID_KEY_STORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(alias)) {
                SecurityRSA.generateKeyPair(context, alias);
            }
            // Even if we just generated the key, always read it back to ensure we
            // can read it successfully.
            final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    alias, null);
            keyPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] wrapPrivateKey(PrivateKey key) {
        try {
            cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());
            return cipher.wrap(key);
        } catch (InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ECPrivateKey unwrapPrivateKey(byte[] blob) throws GeneralSecurityException {
        cipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
        return (ECPrivateKey) cipher.unwrap(blob, StringUtils.ECDSA, Cipher.PRIVATE_KEY);
    }

    public byte[] wrapPublicKey(PublicKey key) throws GeneralSecurityException {
        cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());
        return cipher.wrap(key);
    }

    public ECPublicKey unwrapPublicKey(byte[] blob) throws GeneralSecurityException {
        cipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
        return (ECPublicKey) cipher.unwrap(blob, StringUtils.ECDSA, Cipher.PUBLIC_KEY);
    }

}
