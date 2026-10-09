/*
 * Copyright 2018 m2049r
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.anonero.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyInfo;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import timber.log.Timber;


public class KeyStoreHelper {
    private static final String TAG = "KeyStoreHelper";

    static {
        System.loadLibrary("anonero");
    }

    public static native byte[] slowHash(byte[] data, int brokenVariant);

    static final private String RSA_ALIAS = "MonerujoRSA";

    private static String getCrazyPass(Context context, String password, int brokenVariant) {
        byte[] data = password.getBytes(StandardCharsets.UTF_8);
        byte[] sig = null;
        try {
            KeyStoreHelper.createKeys(context, RSA_ALIAS);
            sig = KeyStoreHelper.signData(data);
            byte[] hash = slowHash(sig, brokenVariant);
            if (hash == null) {
                throw new IllegalStateException("Slow Hash is null!");
            }
            return CrazyPassEncoder.encode(hash);
        } catch (NoSuchProviderException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | KeyStoreException |
                 InvalidKeyException | SignatureException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String getCrazyPass(Context context, String password) {
        return getCrazyPass(context, password, 0);

    }

    public static String getBrokenCrazyPass(Context context, String password, int brokenVariant) {
        // due to a link bug in the initial implementation, some crazypasses were built with
        // prehash & variant == 1
        // since there are wallets out there, we need to keep this here
        // yes, it's a mess
        if (isArm32() && (brokenVariant != 2)) return null;
        return getCrazyPass(context, password, brokenVariant);
    }

    private static Boolean isArm32 = null;

    public static boolean isArm32() {
        if (isArm32 != null) return isArm32;
        synchronized (KeyStoreException.class) {
            if (isArm32 != null) return isArm32;
            isArm32 = Build.SUPPORTED_ABIS[0].equals("armeabi-v7a");
            return isArm32;
        }
    }

    public static void saveWalletUserPass(@NonNull Context context, String wallet, String password) {
        String walletKeyAlias = SecurityConstants.WALLET_PASS_KEY_PREFIX + wallet;
        byte[] data = password.getBytes(StandardCharsets.UTF_8);
        try {
            KeyStoreHelper.createKeys(context, walletKeyAlias);
            byte[] encrypted = KeyStoreHelper.encrypt(walletKeyAlias, data);
            SharedPreferences.Editor e = context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE).edit();
            if (encrypted == null) {
                e.remove(wallet).apply();
                return;
            }
            e.putString(wallet, Base64.encodeToString(encrypted, Base64.DEFAULT)).apply();
        } catch (NoSuchProviderException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | KeyStoreException ex) {
            Timber.tag(TAG).e(ex);
        }
    }

    static public class BrokenPasswordStoreException extends Exception {
        BrokenPasswordStoreException() {
            super();
        }

        BrokenPasswordStoreException(Throwable cause) {
            super(cause);
        }
    }

    public static boolean hasStoredPasswords(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.getAll().isEmpty();
    }

    public static String loadWalletUserPass(@NonNull Context context, String wallet) throws BrokenPasswordStoreException {
        String walletKeyAlias = SecurityConstants.WALLET_PASS_KEY_PREFIX + wallet;
        String encoded = context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(wallet, "");
        if (encoded.isEmpty()) throw new BrokenPasswordStoreException();
        byte[] data = Base64.decode(encoded, Base64.DEFAULT);
        byte[] decrypted = KeyStoreHelper.decrypt(walletKeyAlias, data);
        if (decrypted == null) throw new BrokenPasswordStoreException();
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static void removeWalletUserPass(Context context, String wallet) {
        String walletKeyAlias = SecurityConstants.WALLET_PASS_KEY_PREFIX + wallet;
        try {
            KeyStoreHelper.deleteKeys(walletKeyAlias);
        } catch (KeyStoreException ex) {
            Timber.tag(TAG).e(ex);
        }
        context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE).edit()
                .remove(wallet).apply();
    }

    // --- Wallet secret W, wrapped by an AES-256-GCM key in the Keystore ---------------
    // StrongBox (dedicated secure element) where the device has it, else the TEE, else a
    // software key. The wrapped secret is stored beside the RSA one under a distinct
    // prefix so the two never collide. AES-GCM is authenticated and is the algorithm
    // StrongBox actually supports well; RSA-in-StrongBox is not portable across devices.

    private static final String WALLET_SECRET_PREFIX = "walletSecret-";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private static SecretKey getOrCreateWalletSecretKey(String alias) throws GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        try { ks.load(null); } catch (Exception e) { throw new GeneralSecurityException(e); }
        if (ks.containsAlias(alias)) return (SecretKey) ks.getKey(alias, null);
        // Try StrongBox first (API 28+). A device without it throws, so fall back to
        // TEE/software -- each attempt uses a fresh spec so the StrongBox flag can't leak
        // into the fallback, and a partially-created alias is cleared before retrying.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                return generateWalletSecretKey(alias, true);
            } catch (Exception strongBoxUnavailable) {
                Timber.tag(TAG).i("StrongBox unavailable, using TEE/software key");
                try { ks.deleteEntry(alias); } catch (Exception ignored) { }
            }
        }
        return generateWalletSecretKey(alias, false);
    }

    private static SecretKey generateWalletSecretKey(String alias, boolean strongBox)
            throws GeneralSecurityException {
        KeyGenParameterSpec.Builder b = new KeyGenParameterSpec.Builder(
                alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256);
        if (strongBox) b.setIsStrongBoxBacked(true);
        // the Keystore refuses W while the phone itself is locked: a seized locked phone gives
        // up nothing even to code running as the app. Nothing needs W while locked -- background
        // sync runs on the view key and wallet2 keeps the unwrapped W in memory for its saves.
        // Android 15+ only: 12-14 delete this key when the screen lock is removed, wiping the wallet
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) b.setUnlockedDeviceRequired(true);
        KeyGenerator kg = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        kg.init(b.build());
        return kg.generateKey();
    }

    public static void saveWalletSecret(Context context, String wallet, String secret) throws GeneralSecurityException {
        SecretKey key = getOrCreateWalletSecretKey(WALLET_SECRET_PREFIX + wallet);
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();                 // system-generated for AndroidKeyStore GCM
        byte[] ct = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
        byte[] out = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ct, 0, out, iv.length, ct.length);
        // commit, not apply: callers delete their rollback copy right after this returns,
        // and a wallet re-keyed to a secret that never reached disk cannot be opened again
        boolean written = context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(WALLET_SECRET_PREFIX + wallet, Base64.encodeToString(out, Base64.NO_WRAP)).commit();
        if (!written) throw new GeneralSecurityException("wrapped wallet secret was not written");
    }

    public static String loadWalletSecret(Context context, String wallet) throws GeneralSecurityException {
        String encoded = context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(WALLET_SECRET_PREFIX + wallet, "");
        if (encoded.isEmpty()) throw new GeneralSecurityException("no wallet secret stored");
        byte[] all = Base64.decode(encoded, Base64.NO_WRAP);
        byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_BYTES);
        byte[] ct = Arrays.copyOfRange(all, GCM_IV_BYTES, all.length);
        KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        try { ks.load(null); } catch (Exception e) { throw new GeneralSecurityException(e); }
        SecretKey key = (SecretKey) ks.getKey(WALLET_SECRET_PREFIX + wallet, null);
        if (key == null) throw new GeneralSecurityException("wallet key missing");
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
    }

    public static boolean walletSecretExists(Context context, String wallet) {
        return !context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(WALLET_SECRET_PREFIX + wallet, "").isEmpty();
    }

    public static void removeWalletSecret(Context context, String wallet) {
        try {
            KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            ks.deleteEntry(WALLET_SECRET_PREFIX + wallet);
        } catch (Exception ex) {
            Timber.tag(TAG).e(ex);
        }
        context.getSharedPreferences(SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE)
                .edit().remove(WALLET_SECRET_PREFIX + wallet).apply();
    }

    /** What actually protects W: "strongbox" | "tee" | "software" | "unknown". */
    public static String walletSecretSecurityLevel(String wallet) {
        try {
            KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            SecretKey key = (SecretKey) ks.getKey(WALLET_SECRET_PREFIX + wallet, null);
            if (key == null) return "unknown";
            SecretKeyFactory f = SecretKeyFactory.getInstance(
                    key.getAlgorithm(), SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            KeyInfo info = (KeyInfo) f.getKeySpec(key, KeyInfo.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                switch (info.getSecurityLevel()) {
                    case KeyProperties.SECURITY_LEVEL_STRONGBOX: return "strongbox";
                    case KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT: return "tee";
                    case KeyProperties.SECURITY_LEVEL_SOFTWARE: return "software";
                    default: return "unknown";
                }
            }
            return info.isInsideSecureHardware() ? "tee" : "software";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static void copyWalletUserPass(Context context, String srcWallet, String dstWallet) throws BrokenPasswordStoreException {
        final String pass = loadWalletUserPass(context, srcWallet);
        saveWalletUserPass(context, dstWallet, pass);
    }

    /**
     * Creates a public and private key and stores it using the Android Key
     * Store, so that only this application will be able to access the keys.
     */
    private static void createKeys(Context context, String alias) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
        } catch (IOException | CertificateException ex) {
            throw new IllegalStateException("Could not load KeyStore", ex);
        }
        if (!keyStore.containsAlias(alias)) {
            createKeysM(alias);
        }
    }

    private static void deleteKeys(String alias) throws KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        try {
            keyStore.load(null);
            keyStore.deleteEntry(alias);
        } catch (IOException | NoSuchAlgorithmException | CertificateException ex) {
            Timber.tag(TAG).e(ex);
        }
    }

    public static boolean keyExists(String wallet) throws BrokenPasswordStoreException {
        try {
            KeyStore keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            keyStore.load(null);
            return keyStore.containsAlias(SecurityConstants.WALLET_PASS_KEY_PREFIX + wallet);
        } catch (IOException | NoSuchAlgorithmException | CertificateException |
                 KeyStoreException ex) {
            throw new BrokenPasswordStoreException(ex);
        }
    }

    @SuppressWarnings("deprecation")
    private static void createKeysJBMR2(Context context, String alias) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 300);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.valueOf(Math.abs(alias.hashCode())))
                .setStartDate(start.getTime()).setEndDate(end.getTime())
                .build();
        // defaults to 2048 bit modulus
        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(
                SecurityConstants.TYPE_RSA,
                SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        kpGenerator.initialize(spec);
        KeyPair kp = kpGenerator.generateKeyPair();
        Timber.tag("createKeysJBMR2").d("preM Keys created");
    }

    private static void createKeysM(String alias) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        alias, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .build());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
    }

    private static PrivateKey getPrivateKey(String alias) {
        try {
            KeyStore ks = KeyStore
                    .getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            //KeyStore.Entry entry = ks.getEntry(alias, null);
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, null);

            if (privateKey == null) {
                Timber.tag("createKeysM").i("No key found under alias: %s", alias);

                return null;
            }

            return privateKey;
        } catch (IOException | NoSuchAlgorithmException | CertificateException
                 | UnrecoverableEntryException | KeyStoreException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static PublicKey getPublicKey(String alias) {
        try {
            KeyStore ks = KeyStore
                    .getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);

            return ks.getCertificate(alias).getPublicKey();
        } catch (IOException | NoSuchAlgorithmException | CertificateException
                 | KeyStoreException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static byte[] encrypt(String alias, byte[] data) {
        try {
            PublicKey publicKey = getPublicKey(alias);
            Cipher cipher = Cipher.getInstance(SecurityConstants.CIPHER_RSA_ECB_PKCS1);

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
                 | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Timber.tag(TAG).e(ex);
            return null;
        }
    }

    private static byte[] decrypt(String alias, byte[] data) {
        try {
            PrivateKey privateKey = getPrivateKey(alias);
            if (privateKey == null) return null;
            Cipher cipher = Cipher.getInstance(SecurityConstants.CIPHER_RSA_ECB_PKCS1);

            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException ex) {
            Timber.tag(TAG).e(ex);
            return null;
        }
    }

    /**
     * Signs the data using the key pair stored in the Android Key Store. This
     * signature can be used with the data later to verify it was signed by this
     * application.
     *
     * @return The data signature generated
     */
    private static byte[] signData(byte[] data) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        PrivateKey privateKey = getPrivateKey(KeyStoreHelper.RSA_ALIAS);
        if (privateKey == null) return null;
        Signature s = Signature.getInstance(SecurityConstants.SIGNATURE_SHA256withRSA);
        s.initSign(privateKey);
        s.update(data);
        return s.sign();
    }

    public interface SecurityConstants {
        String KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";
        String TYPE_RSA = "RSA";
        String SIGNATURE_SHA256withRSA = "SHA256withRSA";
        String CIPHER_RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";
        String WALLET_PASS_PREFS_NAME = "wallet";
        String WALLET_PASS_KEY_PREFIX = "walletKey-";
    }
}