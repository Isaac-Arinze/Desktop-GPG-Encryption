package com.cryptomanager.service.model;

/**
 * Result object for encryption operations following SOLID principles.
 * Immutable result containing encrypted data and metadata.
 */
public class EncryptionResult {
    private final byte[] encryptedData;
    private final String algorithm;
    private final String keyId;
    private final long timestamp;
    private final String checksum;

    private EncryptionResult(Builder builder) {
        this.encryptedData = builder.encryptedData;
        this.algorithm = builder.algorithm;
        this.keyId = builder.keyId;
        this.timestamp = builder.timestamp;
        this.checksum = builder.checksum;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
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

    public String getChecksum() {
        return checksum;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte[] encryptedData;
        private String algorithm;
        private String keyId;
        private long timestamp = System.currentTimeMillis();
        private String checksum;

        public Builder encryptedData(byte[] encryptedData) {
            this.encryptedData = encryptedData;
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

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public EncryptionResult build() {
            return new EncryptionResult(this);
        }
    }
}
