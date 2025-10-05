package com.cryptomanager.service;

import com.cryptomanager.service.model.EncryptionOptions;
import com.cryptomanager.service.model.EncryptionResult;
import com.cryptomanager.service.model.DecryptionResult;
import com.cryptomanager.service.exception.CryptoServiceException;

/**
 * Encryption service interface following SOLID principles.
 * Handles encryption and decryption operations separately from other crypto functions.
 */
public interface EncryptionService {

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
     * Encrypts data with default options.
     *
     * @param data The data to encrypt
     * @param algorithm The encryption algorithm to use
     * @return EncryptionResult containing encrypted data and metadata
     * @throws CryptoServiceException if encryption fails
     */
    EncryptionResult encrypt(byte[] data, String algorithm) throws CryptoServiceException;

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
     * Checks if the specified encryption algorithm is supported.
     *
     * @param algorithm The algorithm to check
     * @return true if the algorithm is supported, false otherwise
     */
    boolean isAlgorithmSupported(String algorithm);

    /**
     * Gets the default encryption options for the specified algorithm.
     *
     * @param algorithm The encryption algorithm
     * @return EncryptionOptions with default settings
     */
    EncryptionOptions getDefaultOptions(String algorithm);

    /**
     * Encrypts a file and saves the result to another file.
     *
     * @param inputFile Path to the input file
     * @param outputFile Path for the encrypted output file
     * @param algorithm The encryption algorithm to use
     * @param options Encryption options
     * @throws CryptoServiceException if file encryption fails
     */
    void encryptFile(String inputFile, String outputFile, String algorithm, EncryptionOptions options)
            throws CryptoServiceException;

    /**
     * Decrypts a file and saves the result to another file.
     *
     * @param inputFile Path to the encrypted input file
     * @param outputFile Path for the decrypted output file
     * @param algorithm The encryption algorithm used
     * @param key The decryption key
     * @throws CryptoServiceException if file decryption fails
     */
    void decryptFile(String inputFile, String outputFile, String algorithm, String key)
            throws CryptoServiceException;
}
