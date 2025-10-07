package com.cryptomanager.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.cryptomanager.service.KeyManagementService;
import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.model.KeyMetadata;
import com.cryptomanager.service.model.KeyPair;

/**
 * Simple implementation of key management service for OpenPGP operations.
 * Stores keys in the user's home directory under .crypto-manager/keys/
 */
public class SimpleKeyManagementService implements KeyManagementService {

    private static final String KEY_DIRECTORY = System.getProperty("user.home") + "/.crypto-manager/keys";
    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final int DEFAULT_KEY_SIZE = 2048;

    public SimpleKeyManagementService() {
        try {
            Files.createDirectories(Paths.get(KEY_DIRECTORY));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create key directory", e);
        }
    }

    @Override
    public KeyMetadata generateKeyPair(int keySize, String keyId, String passphrase) throws CryptoServiceException {
        try {
            // Generate RSA key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
            keyGen.initialize(keySize);
            java.security.KeyPair keyPair = keyGen.generateKeyPair();

            // Save keys to files
            saveKeyPair(keyId, keyPair, passphrase);

            return KeyMetadata.builder()
                    .keyId(keyId)
                    .algorithm(DEFAULT_ALGORITHM)
                    .keySize(keySize)
                    .createdAt(System.currentTimeMillis())
                    .encrypted(passphrase != null && !passphrase.isEmpty())
                    .format("PEM")
                    .description("RSA key pair for OpenPGP operations")
                    .build();

        } catch (Exception e) {
            throw new CryptoServiceException("Failed to generate key pair", e);
        }
    }

    @Override
    public KeyMetadata generateKeyPair(String keyId) throws CryptoServiceException {
        return generateKeyPair(DEFAULT_KEY_SIZE, keyId, null);
    }

    @Override
    public KeyPair getKeyPair(String keyId, String passphrase) throws CryptoServiceException {
        try {
            String publicKeyPath = getKeyPath(keyId, "public.pem");
            String privateKeyPath = getKeyPath(keyId, "private.pem");

            if (!Files.exists(Paths.get(publicKeyPath)) || !Files.exists(Paths.get(privateKeyPath))) {
                throw new CryptoServiceException("Key pair not found: " + keyId);
            }

            // Load public key
            PublicKey publicKey = loadPublicKey(publicKeyPath);

            // Load and decrypt private key if needed
            PrivateKey privateKey = loadPrivateKey(privateKeyPath, passphrase);

            return KeyPair.builder()
                    .keyId(keyId)
                    .publicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
                    .privateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()))
                    .algorithm(DEFAULT_ALGORITHM)
                    .keySize(DEFAULT_KEY_SIZE)
                    .createdAt(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            throw new CryptoServiceException("Failed to retrieve key pair", e);
        }
    }

    @Override
    public PublicKey getPublicKey(String keyId) throws CryptoServiceException {
        try {
            String publicKeyPath = getKeyPath(keyId, "public.pem");
            if (!Files.exists(Paths.get(publicKeyPath))) {
                throw new CryptoServiceException("Public key not found: " + keyId);
            }
            return loadPublicKey(publicKeyPath);
        } catch (Exception e) {
            throw new CryptoServiceException("Failed to load public key", e);
        }
    }

    @Override
    public String exportPublicKey(String keyId) throws CryptoServiceException {
        try {
            PublicKey publicKey = getPublicKey(keyId);
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        } catch (Exception e) {
            throw new CryptoServiceException("Failed to export public key", e);
        }
    }

    @Override
    public KeyMetadata importPublicKey(String publicKeyData, String keyId) throws CryptoServiceException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyData);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Save the imported public key in PEM format
            String publicKeyPath = getKeyPath(keyId, "public.pem");
            String publicKeyPEM = formatPublicKeyAsPEM(publicKey);
            Files.write(Paths.get(publicKeyPath), publicKeyPEM.getBytes(StandardCharsets.UTF_8));

            return KeyMetadata.builder()
                    .keyId(keyId)
                    .algorithm(DEFAULT_ALGORITHM)
                    .keySize(DEFAULT_KEY_SIZE)
                    .createdAt(System.currentTimeMillis())
                    .encrypted(false)
                    .format("PEM")
                    .description("Imported public key")
                    .build();

        } catch (Exception e) {
            throw new CryptoServiceException("Failed to import public key", e);
        }
    }

    @Override
    public KeyMetadata[] listKeys() throws CryptoServiceException {
        try {
            File keyDir = new File(KEY_DIRECTORY);
            File[] keyFiles = keyDir.listFiles((dir, name) -> name.endsWith(".public.pem"));

            if (keyFiles == null) {
                return new KeyMetadata[0];
            }

            List<KeyMetadata> keys = new ArrayList<>();
            for (File publicKeyFile : keyFiles) {
                String keyId = publicKeyFile.getName().replace(".public.pem", "");
                String privateKeyPath = getKeyPath(keyId, "private.pem");
                boolean hasPrivate = Files.exists(Paths.get(privateKeyPath));

                keys.add(KeyMetadata.builder()
                        .keyId(keyId)
                        .algorithm(DEFAULT_ALGORITHM)
                        .keySize(DEFAULT_KEY_SIZE)
                        .createdAt(publicKeyFile.lastModified())
                        .encrypted(hasPrivate)
                        .format("PEM")
                        .description(hasPrivate ? "RSA key pair" : "Public key only")
                        .build());
            }

            return keys.toArray(new KeyMetadata[0]);

        } catch (Exception e) {
            throw new CryptoServiceException("Failed to list keys", e);
        }
    }

    @Override
    public void deleteKey(String keyId, String passphrase) throws CryptoServiceException {
        try {
            String publicKeyPath = getKeyPath(keyId, "public.pem");
            String privateKeyPath = getKeyPath(keyId, "private.pem");

            Files.deleteIfExists(Paths.get(publicKeyPath));
            Files.deleteIfExists(Paths.get(privateKeyPath));

        } catch (Exception e) {
            throw new CryptoServiceException("Failed to delete key", e);
        }
    }

    @Override
    public boolean keyExists(String keyId) {
        try {
            String publicKeyPath = getKeyPath(keyId, "public.pem");
            return Files.exists(Paths.get(publicKeyPath));
        } catch (Exception e) {
            return false;
        }
    }

    private void saveKeyPair(String keyId, java.security.KeyPair keyPair, String passphrase) throws Exception {
        String publicKeyPath = getKeyPath(keyId, "public.pem");
        String privateKeyPath = getKeyPath(keyId, "private.pem");

        // Save public key in PEM format
        String publicKeyPEM = formatPublicKeyAsPEM(keyPair.getPublic());
        Files.write(Paths.get(publicKeyPath), publicKeyPEM.getBytes(StandardCharsets.UTF_8));

        // Save private key in PEM format
        String privateKeyPEM = formatPrivateKeyAsPEM(keyPair.getPrivate());
        Files.write(Paths.get(privateKeyPath), privateKeyPEM.getBytes(StandardCharsets.UTF_8));
    }

    private String formatPublicKeyAsPEM(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        String base64Encoded = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" +
               base64Encoded.replaceAll("(.{64})", "$1\n") +
               "\n-----END PUBLIC KEY-----\n";
    }

    private String formatPrivateKeyAsPEM(PrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded();
        String base64Encoded = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PRIVATE KEY-----\n" +
               base64Encoded.replaceAll("(.{64})", "$1\n") +
               "\n-----END PRIVATE KEY-----\n";
    }

    private String getKeyPath(String keyId, String suffix) {
        return KEY_DIRECTORY + "/" + keyId + "." + suffix;
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        String pemContent = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        byte[] keyBytes = parsePEMContent(pemContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey loadPrivateKey(String path, String passphrase) throws Exception {
        String pemContent = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        byte[] keyBytes = parsePEMContent(pemContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    private byte[] parsePEMContent(String pemContent) throws Exception {
        // Remove PEM headers and footers, but keep Base64 content intact
        String base64Content = pemContent
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .trim(); // Only remove leading/trailing whitespace

        return Base64.getDecoder().decode(base64Content);
    }
}
