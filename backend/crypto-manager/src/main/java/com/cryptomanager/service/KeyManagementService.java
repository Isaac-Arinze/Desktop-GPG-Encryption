package com.cryptomanager.service;

import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.model.KeyMetadata;
import com.cryptomanager.service.model.KeyPair;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Key management service interface following SOLID principles.
 * Handles all key-related operations including generation, storage, and retrieval.
 */
public interface KeyManagementService {

    /**
     * Generates a new RSA key pair for OpenPGP operations.
     *
     * @param keySize The key size in bits (e.g., 2048, 4096)
     * @param keyId Unique identifier for the key pair
     * @param passphrase Optional passphrase for private key encryption
     * @return KeyMetadata containing key information
     * @throws CryptoServiceException if key generation fails
     */
    KeyMetadata generateKeyPair(int keySize, String keyId, String passphrase) throws CryptoServiceException;

    /**
     * Generates a new RSA key pair with default settings.
     *
     * @param keyId Unique identifier for the key pair
     * @return KeyMetadata containing key information
     * @throws CryptoServiceException if key generation fails
     */
    KeyMetadata generateKeyPair(String keyId) throws CryptoServiceException;

    /**
     * Retrieves a key pair by its ID.
     *
     * @param keyId The key identifier
     * @param passphrase Passphrase for private key decryption
     * @return KeyPair containing public and private keys
     * @throws CryptoServiceException if key retrieval fails
     */
    KeyPair getKeyPair(String keyId, String passphrase) throws CryptoServiceException;

    /**
     * Retrieves the public key for a given key ID.
     *
     * @param keyId The key identifier
     * @return PublicKey for encryption operations
     * @throws CryptoServiceException if public key retrieval fails
     */
    PublicKey getPublicKey(String keyId) throws CryptoServiceException;

    /**
     * Exports a public key in OpenPGP-compatible format.
     *
     * @param keyId The key identifier
     * @return Base64-encoded public key
     * @throws CryptoServiceException if export fails
     */
    String exportPublicKey(String keyId) throws CryptoServiceException;

    /**
     * Imports a public key from OpenPGP format.
     *
     * @param publicKeyData Base64-encoded public key data
     * @param keyId Identifier for the imported key
     * @return KeyMetadata for the imported key
     * @throws CryptoServiceException if import fails
     */
    KeyMetadata importPublicKey(String publicKeyData, String keyId) throws CryptoServiceException;

    /**
     * Lists all available key pairs.
     *
     * @return Array of KeyMetadata for all keys
     * @throws CryptoServiceException if listing fails
     */
    KeyMetadata[] listKeys() throws CryptoServiceException;

    /**
     * Deletes a key pair.
     *
     * @param keyId The key identifier to delete
     * @param passphrase Passphrase for authorization
     * @throws CryptoServiceException if deletion fails
     */
    void deleteKey(String keyId, String passphrase) throws CryptoServiceException;

    /**
     * Checks if a key pair exists.
     *
     * @param keyId The key identifier to check
     * @return true if the key exists, false otherwise
     */
    boolean keyExists(String keyId);
}
