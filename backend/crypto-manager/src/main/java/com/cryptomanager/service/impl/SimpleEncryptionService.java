package com.cryptomanager.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.cryptomanager.service.EncryptionService;
import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.model.DecryptionResult;
import com.cryptomanager.service.model.EncryptionOptions;
import com.cryptomanager.service.model.EncryptionResult;

/**
 * Minimal AES-GCM encryption-only service.
 */
public class SimpleEncryptionService implements EncryptionService {

    private static final String SUPPORTED_ALGO = "AES-GCM";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final int PBKDF2_ITERATIONS = 150_000;

    @Override
    public EncryptionResult encrypt(byte[] data, String algorithm, EncryptionOptions options)
            throws CryptoServiceException {
        try {
            if (!isAlgorithmSupported(algorithm)) {
                throw new CryptoServiceException("Unsupported algorithm: " + algorithm);
            }
            // Derive key from provided keyId as passphrase for demo purposes
            String passphrase = options != null && options.getKeyId() != null ? options.getKeyId() : "default-passphrase";
            byte[] salt = randomBytes(SALT_BYTES);
            SecretKey key = deriveKey(passphrase.toCharArray(), salt);
            byte[] iv = randomBytes(IV_BYTES);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(data);

            // output format: salt || iv || ciphertext
            byte[] out = new byte[salt.length + iv.length + ciphertext.length];
            System.arraycopy(salt, 0, out, 0, salt.length);
            System.arraycopy(iv, 0, out, salt.length, iv.length);
            System.arraycopy(ciphertext, 0, out, salt.length + iv.length, ciphertext.length);

            return EncryptionResult.builder()
                    .encryptedData(out)
                    .algorithm(SUPPORTED_ALGO)
                    .keyId("derived")
                    .build();
        } catch (Exception e) {
            throw new CryptoServiceException("Encryption failed", e);
        }
    }

    @Override
    public EncryptionResult encrypt(byte[] data, String algorithm) throws CryptoServiceException {
        return encrypt(data, algorithm, EncryptionOptions.builder().keyId("default-passphrase").build());
    }

    @Override
    public DecryptionResult decrypt(byte[] encryptedData, String algorithm, String key) throws CryptoServiceException {
        try {
            if (!isAlgorithmSupported(algorithm)) {
                throw new CryptoServiceException("Unsupported algorithm: " + algorithm);
            }

            if (encryptedData.length < SALT_BYTES + IV_BYTES) {
                throw new CryptoServiceException("Invalid encrypted data format");
            }

            // Extract salt, IV, and ciphertext from the encrypted data
            // Format: salt(16) || iv(12) || ciphertext(n)
            byte[] salt = Arrays.copyOfRange(encryptedData, 0, SALT_BYTES);
            byte[] iv = Arrays.copyOfRange(encryptedData, SALT_BYTES, SALT_BYTES + IV_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(encryptedData, SALT_BYTES + IV_BYTES, encryptedData.length);

            // Derive the same key using the extracted salt
            SecretKey aesKey = deriveKey(key.toCharArray(), salt);

            // Decrypt the data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] decryptedData = cipher.doFinal(ciphertext);

            return DecryptionResult.builder()
                    .decryptedData(decryptedData)
                    .algorithm(SUPPORTED_ALGO)
                    .build();

        } catch (Exception e) {
            throw new CryptoServiceException("Decryption failed", e);
        }
    }

    @Override
    public boolean isAlgorithmSupported(String algorithm) {
        return SUPPORTED_ALGO.equalsIgnoreCase(algorithm) || "AES/GCM/NoPadding".equalsIgnoreCase(algorithm);
    }

    @Override
    public EncryptionOptions getDefaultOptions(String algorithm) {
        return EncryptionOptions.builder().keyId("default-passphrase").useCompression(false).build();
    }

    @Override
    public void encryptFile(String inputFile, String outputFile, String algorithm, EncryptionOptions options) throws CryptoServiceException {
        try {
            byte[] in = Files.readAllBytes(Path.of(inputFile));
            EncryptionResult res = encrypt(in, algorithm, options);
            Files.write(Path.of(outputFile), res.getEncryptedData());
        } catch (Exception e) {
            throw new CryptoServiceException("File encryption failed", e);
        }
    }

    @Override
    public void decryptFile(String inputFile, String outputFile, String algorithm, String key) throws CryptoServiceException {
        throw new CryptoServiceException("Decrypt not supported in SimpleEncryptionService");
    }

    private static SecretKey deriveKey(char[] passphrase, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        Arrays.fill(keyBytes, (byte) 0);
        return secretKey;
    }

    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        new SecureRandom().nextBytes(b);
        return b;
    }
}
