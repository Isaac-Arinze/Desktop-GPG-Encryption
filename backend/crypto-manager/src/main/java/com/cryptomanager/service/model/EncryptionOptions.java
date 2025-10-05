package com.cryptomanager.service.model;

import java.security.PublicKey;

/**
 * Options for encryption operations following SOLID principles.
 * Immutable configuration object for encryption parameters.
 */
public class EncryptionOptions {
    private final String keyId;
    private final boolean useCompression;
    private final String compressionAlgorithm;
    private final PublicKey publicKey;
    private final boolean asciiArmor;

    private EncryptionOptions(Builder builder) {
        this.keyId = builder.keyId;
        this.useCompression = builder.useCompression;
        this.compressionAlgorithm = builder.compressionAlgorithm;
        this.publicKey = builder.publicKey;
        this.asciiArmor = builder.asciiArmor;
    }

    public String getKeyId() {
        return keyId;
    }

    public boolean isUseCompression() {
        return useCompression;
    }

    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public boolean isAsciiArmor() {
        return asciiArmor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyId;
        private boolean useCompression = false;
        private String compressionAlgorithm = "ZIP";
        private PublicKey publicKey;
        private boolean asciiArmor = false;

        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }

        public Builder useCompression(boolean useCompression) {
            this.useCompression = useCompression;
            return this;
        }

        public Builder compressionAlgorithm(String compressionAlgorithm) {
            this.compressionAlgorithm = compressionAlgorithm;
            return this;
        }

        public Builder publicKey(PublicKey publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder asciiArmor(boolean asciiArmor) {
            this.asciiArmor = asciiArmor;
            return this;
        }

        public EncryptionOptions build() {
            return new EncryptionOptions(this);
        }
    }
}
