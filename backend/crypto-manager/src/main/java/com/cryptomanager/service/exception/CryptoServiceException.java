package com.cryptomanager.service.exception;

/**
 * Exception thrown by cryptographic service operations following SOLID principles.
 * Provides specific error information for different failure scenarios.
 */
public class CryptoServiceException extends Exception {
    private final String operation;
    private final String algorithm;
    private final ErrorCode errorCode;

    public enum ErrorCode {
        ENCRYPTION_FAILED,
        DECRYPTION_FAILED,
        SIGNATURE_FAILED,
        VERIFICATION_FAILED,
        KEY_NOT_FOUND,
        INVALID_KEY,
        INVALID_DATA,
        ALGORITHM_NOT_SUPPORTED,
        IO_ERROR,
        SECURITY_ERROR
    }

    public CryptoServiceException(String message) {
        super(message);
        this.operation = null;
        this.algorithm = null;
        this.errorCode = null;
    }

    public CryptoServiceException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.algorithm = null;
        this.errorCode = null;
    }

    public CryptoServiceException(String message, String operation, String algorithm, ErrorCode errorCode) {
        super(message);
        this.operation = operation;
        this.algorithm = algorithm;
        this.errorCode = errorCode;
    }

    public CryptoServiceException(String message, Throwable cause, String operation, String algorithm, ErrorCode errorCode) {
        super(message, cause);
        this.operation = operation;
        this.algorithm = algorithm;
        this.errorCode = errorCode;
    }

    public String getOperation() {
        return operation;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CryptoServiceException");

        if (errorCode != null) {
            sb.append(" [").append(errorCode).append("]");
        }

        if (operation != null) {
            sb.append(" during ").append(operation);
        }

        if (algorithm != null) {
            sb.append(" with algorithm ").append(algorithm);
        }

        sb.append(": ").append(getMessage());

        return sb.toString();
    }
}
