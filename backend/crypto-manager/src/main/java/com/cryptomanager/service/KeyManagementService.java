package com.cryptomanager.service;

import com.cryptomanager.service.model.KeyPair;
import com.cryptomanager.service.model.KeyMetadata;
import com.cryptomanager.service.exception.CryptoServiceException;

/**
 * Key management service interface following SOLID principles.
 * Handles all key-related operations including generation, storage, and retrieval.
 */
public interface KeyManagementService {

    /**
     * Generates a new key pair using the specified algorithm.
     *
     * @param algorithm The key algorithm to use (RSA, ECC, etc.)
     * @param keySize The key size in bits
     * @param keyId Optional identifier for the key pair
     * @return KeyPair containing public and private keys with metadata
     * @throws CryptoServiceException if key generation fails
     */
    KeyPair generateKeyPair(String algorithm, int keySize, String keyId)
            throws CryptoServiceException;

    /**
     * Generates a new key pair with default parameters.
     *
     * @param algorithm The key algorithm to use
     * @return KeyPair containing public and private keys with metadata
     * @throws CryptoServiceException if key generation fails
     */
    KeyPair generateKeyPair(String algorithm) throws CryptoServiceException;

    /**
     * Stores a key pair securely.
     *
     * @param keyPair The key pair to store
     * @param passphrase Optional passphrase for encryption
     * @return KeyMetadata containing storage information
     * @throws CryptoServiceException if storage fails
     */
    KeyMetadata storeKeyPair(KeyPair keyPair, String passphrase)
            throws CryptoServiceException;

    /**
     * Retrieves a key pair by its identifier.
     *
     * @param keyId The key identifier
     * @param passphrase Passphrase if the key was encrypted
     * @return KeyPair containing the requested keys
     * @throws CryptoServiceException if retrieval fails
     */
    KeyPair retrieveKeyPair(String keyId, String passphrase)
            throws CryptoServiceException;

    /**
     * Deletes a key pair by its identifier.
     *
     * @param keyId The key identifier to delete
     * @throws CryptoServiceException if deletion fails
     */
    void deleteKeyPair(String keyId) throws CryptoServiceException;

    /**
     * Lists all available key pairs.
     *
     * @return Array of KeyMetadata for all stored keys
     * @throws CryptoServiceException if listing fails
     */
    KeyMetadata[] listKeys() throws CryptoServiceException;

    /**
     * Exports a public key in the specified format.
     *
     * @param keyId The key identifier
     * @param format The export format (PEM, DER, etc.)
     * @return String containing the exported public key
     * @throws CryptoServiceException if export fails
     */
    String exportPublicKey(String keyId, String format)
            throws CryptoServiceException;

    /**
     * Imports a key pair from external source.
     *
     * @param keyData The key data to import
     * @param format The format of the key data
     * @param keyId Optional identifier for the imported key
     * @return KeyMetadata for the imported key
     * @throws CryptoServiceException if import fails
     */
    KeyMetadata importKey(String keyData, String format, String keyId)
            throws CryptoServiceException;
}
