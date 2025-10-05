package com.cryptomanager.service;

import com.cryptomanager.service.model.SignatureResult;
import com.cryptomanager.service.model.VerificationResult;
import com.cryptomanager.service.exception.CryptoServiceException;

/**
 * Signature service interface following SOLID principles.
 * Handles digital signature operations separately from other crypto functions.
 */
public interface SignatureService {

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
     * Creates a digital signature with default algorithm.
     *
     * @param data The data to sign
     * @param privateKey The private key for signing
     * @return SignatureResult containing the signature and metadata
     * @throws CryptoServiceException if signing fails
     */
    SignatureResult sign(byte[] data, String privateKey) throws CryptoServiceException;

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

    /**
     * Verifies a signature with specified algorithm.
     *
     * @param data The original data
     * @param signature The signature to verify
     * @param publicKey The public key for verification
     * @param algorithm The signature algorithm used
     * @return VerificationResult indicating if signature is valid
     * @throws CryptoServiceException if verification fails
     */
    VerificationResult verify(byte[] data, byte[] signature, String publicKey, String algorithm)
            throws CryptoServiceException;

    /**
     * Checks if the specified signature algorithm is supported.
     *
     * @param algorithm The algorithm to check
     * @return true if the algorithm is supported, false otherwise
     */
    boolean isAlgorithmSupported(String algorithm);

    /**
     * Gets the default signature algorithm for the specified key type.
     *
     * @param keyAlgorithm The key algorithm (RSA, ECC, etc.)
     * @return The default signature algorithm
     */
    String getDefaultSignatureAlgorithm(String keyAlgorithm);

    /**
     * Signs a file and saves the signature to another file.
     *
     * @param inputFile Path to the input file to sign
     * @param signatureFile Path for the signature output file
     * @param privateKey The private key for signing
     * @param algorithm The signature algorithm to use
     * @throws CryptoServiceException if file signing fails
     */
    void signFile(String inputFile, String signatureFile, String privateKey, String algorithm)
            throws CryptoServiceException;

    /**
     * Verifies a file signature.
     *
     * @param dataFile Path to the original data file
     * @param signatureFile Path to the signature file
     * @param publicKey The public key for verification
     * @return VerificationResult indicating if signature is valid
     * @throws CryptoServiceException if verification fails
     */
    VerificationResult verifyFile(String dataFile, String signatureFile, String publicKey)
            throws CryptoServiceException;
}
