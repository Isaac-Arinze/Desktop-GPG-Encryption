package com.cryptomanager.service;

import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.model.DecryptionResult;
import com.cryptomanager.service.model.EncryptionOptions;
import com.cryptomanager.service.model.EncryptionResult;
import com.cryptomanager.service.model.SignatureResult;
import com.cryptomanager.service.model.VerificationResult;

/**
 * Core cryptographic service interface following SOLID principles.
 * Defines the main contract for all cryptographic operations.
 */
public interface CryptoService {

    /**
     * Encrypts data using the specified algorithm and options.
     *
     * @param data The data to encrypt
     * @param algorithm The encryption algorithm to use
     * @param options Additional encryption options
     * @return EncryptionResult containing encrypted data and metadata
     * @throws CryptoServiceException if encryption fails
     */
    EncryptionResult encrypt(byte[] data, String algorithm, EncryptionOptions options)
            throws CryptoServiceException;

    /**
     * Decrypts data using the specified algorithm and key.
     *
     * @param encryptedData The encrypted data to decrypt
     * @param algorithm The encryption algorithm used
     * @param key The decryption key
     * @return DecryptionResult containing decrypted data
     * @throws CryptoServiceException if decryption fails
     */
    DecryptionResult decrypt(byte[] encryptedData, String algorithm, String key)
            throws CryptoServiceException;

    /**
     * Creates a digital signature for the given data.
     *
     * @param data The data to sign
     * @param privateKey The private key for signing
     * @param signatureAlgorithm The signature algorithm to use
     * @return SignatureResult containing the signature and metadata
     * @throws CryptoServiceException if signing fails
     */
    SignatureResult sign(byte[] data, String privateKey, String signatureAlgorithm)
            throws CryptoServiceException;

    /**
     * Verifies a digital signature against the original data.
     *
     * @param data The original data
     * @param signature The signature to verify
     * @param publicKey The public key for verification
     * @return VerificationResult indicating if signature is valid
     * @throws CryptoServiceException if verification fails
     */
    VerificationResult verify(byte[] data, byte[] signature, String publicKey)
            throws CryptoServiceException;
}
