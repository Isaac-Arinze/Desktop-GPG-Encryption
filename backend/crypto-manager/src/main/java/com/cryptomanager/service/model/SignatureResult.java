package com.cryptomanager.service.model;

/**
 * Result object for signature operations following SOLID principles.
 * Immutable result containing signature data and metadata.
 */
public class SignatureResult {
    private final byte[] signature;
    private final String algorithm;
    private final String keyId;
    private final long timestamp;
    private final String signatureFormat;

    private SignatureResult(Builder builder) {
        this.signature = builder.signature;
        this.algorithm = builder.algorithm;
        this.keyId = builder.keyId;
        this.timestamp = builder.timestamp;
        this.signatureFormat = builder.signatureFormat;
    }

    public byte[] getSignature() {
        return signature;
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

    public String getSignatureFormat() {
        return signatureFormat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte[] signature;
        private String algorithm;
        private String keyId;
        private long timestamp = System.currentTimeMillis();
        private String signatureFormat = "DETACHED";

        public Builder signature(byte[] signature) {
            this.signature = signature;
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

        public Builder signatureFormat(String signatureFormat) {
            this.signatureFormat = signatureFormat;
            return this;
        }

        public SignatureResult build() {
            return new SignatureResult(this);
        }
    }
}
