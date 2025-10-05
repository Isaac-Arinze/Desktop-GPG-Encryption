package com.cryptomanager.service.model;

/**
 * Result object for decryption operations following SOLID principles.
 * Immutable result containing decrypted data and metadata.
 */
public class DecryptionResult {
    private final byte[] decryptedData;
    private final String algorithm;
    private final String keyId;
    private final long timestamp;
    private final boolean integrityVerified;

    private DecryptionResult(Builder builder) {
        this.decryptedData = builder.decryptedData;
        this.algorithm = builder.algorithm;
        this.keyId = builder.keyId;
        this.timestamp = builder.timestamp;
        this.integrityVerified = builder.integrityVerified;
    }

    public byte[] getDecryptedData() {
        return decryptedData;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getKeyId() {
        return keyId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isIntegrityVerified() {
        return integrityVerified;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte[] decryptedData;
        private String algorithm;
        private String keyId;
        private long timestamp = System.currentTimeMillis();
        private boolean integrityVerified = false;

        public Builder decryptedData(byte[] decryptedData) {
            this.decryptedData = decryptedData;
            return this;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder integrityVerified(boolean integrityVerified) {
            this.integrityVerified = integrityVerified;
            return this;
        }

        public DecryptionResult build() {
            return new DecryptionResult(this);
        }
    }
}
