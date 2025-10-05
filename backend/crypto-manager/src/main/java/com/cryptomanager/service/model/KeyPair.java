package com.cryptomanager.service.model;

/**
 * Key pair model following SOLID principles.
 * Immutable container for public and private key data.
 */
public class KeyPair {
    private final String keyId;
    private final String publicKey;
    private final String privateKey;
    private final String algorithm;
    private final int keySize;
    private final long createdAt;
    private final KeyMetadata metadata;

    private KeyPair(Builder builder) {
        this.keyId = builder.keyId;
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
        this.algorithm = builder.algorithm;
        this.keySize = builder.keySize;
        this.createdAt = builder.createdAt;
        this.metadata = builder.metadata;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
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

    public KeyMetadata getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyId;
        private String publicKey;
        private String privateKey;
        private String algorithm;
        private int keySize;
        private long createdAt = System.currentTimeMillis();
        private KeyMetadata metadata;

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
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

        public Builder metadata(KeyMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public KeyPair build() {
            return new KeyPair(this);
        }
    }
}
