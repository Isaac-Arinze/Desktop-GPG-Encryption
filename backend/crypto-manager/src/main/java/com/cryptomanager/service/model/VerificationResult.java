package com.cryptomanager.service.model;

/**
 * Result object for signature verification operations following SOLID principles.
 * Immutable result containing verification status and metadata.
 */
public class VerificationResult {
    private final boolean valid;
    private final String algorithm;
    private final String keyId;
    private final long timestamp;
    private final String message;

    private VerificationResult(Builder builder) {
        this.valid = builder.valid;
        this.algorithm = builder.algorithm;
        this.keyId = builder.keyId;
        this.timestamp = builder.timestamp;
        this.message = builder.message;
    }

    public boolean isValid() {
        return valid;
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

    public String getMessage() {
        return message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid;
        private String algorithm;
        private String keyId;
        private long timestamp = System.currentTimeMillis();
        private String message;

        public Builder valid(boolean valid) {
            this.valid = valid;
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

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public VerificationResult build() {
            return new VerificationResult(this);
        }
    }
}
