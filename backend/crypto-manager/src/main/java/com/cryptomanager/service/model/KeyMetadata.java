package com.cryptomanager.service.model;

/**
 * Metadata for cryptographic keys following SOLID principles.
 * Immutable metadata container for key information.
 */
public class KeyMetadata {
    private final String keyId;
    private final String algorithm;
    private final int keySize;
    private final long createdAt;
    private final long lastUsedAt;
    private final boolean encrypted;
    private final String format;
    private final String description;

    private KeyMetadata(Builder builder) {
        this.keyId = builder.keyId;
        this.algorithm = builder.algorithm;
        this.keySize = builder.keySize;
        this.createdAt = builder.createdAt;
        this.lastUsedAt = builder.lastUsedAt;
        this.encrypted = builder.encrypted;
        this.format = builder.format;
        this.description = builder.description;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getKeySize() {
        return keySize;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastUsedAt() {
        return lastUsedAt;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public String getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyId;
        private String algorithm;
        private int keySize;
        private long createdAt = System.currentTimeMillis();
        private long lastUsedAt;
        private boolean encrypted = false;
        private String format = "PEM";
        private String description;

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder keySize(int keySize) {
            this.keySize = keySize;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastUsedAt(long lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder encrypted(boolean encrypted) {
            this.encrypted = encrypted;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public KeyMetadata build() {
            return new KeyMetadata(this);
        }
    }
}
