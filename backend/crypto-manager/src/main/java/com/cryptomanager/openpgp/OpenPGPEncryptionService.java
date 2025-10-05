package com.cryptomanager.openpgp;

import com.cryptomanager.service.EncryptionService;
import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.model.DecryptionResult;
import com.cryptomanager.service.model.EncryptionOptions;
import com.cryptomanager.service.model.EncryptionResult;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.CRC32;

/**
 * OpenPGP-compatible encryption service implementing RFC 4880.
 * Uses hybrid encryption: RSA for session key, AES for data.
 */
public class OpenPGPEncryptionService implements EncryptionService {

    private static final String SUPPORTED_ALGO = "AES-256";
    private static final String SESSION_KEY_ALGO = "AES";
    private static final int SESSION_KEY_SIZE = 256;
    private static final String ASYMMETRIC_ALGO = "RSA";
    private static final int ASYMMETRIC_KEY_SIZE = 2048;

    @Override
    public EncryptionResult encrypt(byte[] data, String algorithm, EncryptionOptions options)
            throws CryptoServiceException {
        try {
            if (!isAlgorithmSupported(algorithm)) {
                throw new CryptoServiceException("Unsupported algorithm: " + algorithm);
            }

            // Generate session key for AES encryption
            KeyGenerator keyGen = KeyGenerator.getInstance(SESSION_KEY_ALGO);
            keyGen.init(SESSION_KEY_SIZE);
            SecretKey sessionKey = keyGen.generateKey();

            // Generate IV for AES-CFB
            byte[] iv = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            // Encrypt data with session key using AES-CFB
            Cipher aesCipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, new IvParameterSpec(iv));
            byte[] encryptedData = aesCipher.doFinal(data);

            // Create OpenPGP packet structure
            ByteArrayOutputStream packetStream = new ByteArrayOutputStream();

            // 1. Public Key Encrypted Session Key Packet (Tag 1)
            if (options != null && options.getPublicKey() != null) {
                byte[] pkESKPacket = createPKESKPacket(options.getPublicKey(), sessionKey);
                packetStream.write(pkESKPacket);
            }

            // 2. Symmetrically Encrypted Data Packet (Tag 9)
            byte[] sedPacket = createSEDPacket(sessionKey, iv, encryptedData);
            packetStream.write(sedPacket);

            byte[] openPGPData = packetStream.toByteArray();

            return EncryptionResult.builder()
                    .encryptedData(openPGPData)
                    .algorithm("OpenPGP-" + algorithm)
                    .keyId("openpgp-hybrid")
                    .build();

        } catch (Exception e) {
            throw new CryptoServiceException("OpenPGP encryption failed", e);
        }
    }

    @Override
    public EncryptionResult encrypt(byte[] data, String algorithm) throws CryptoServiceException {
        return encrypt(data, algorithm, EncryptionOptions.builder().build());
    }

    @Override
    public DecryptionResult decrypt(byte[] encryptedData, String algorithm, String key) throws CryptoServiceException {
        try {
            if (!isAlgorithmSupported(algorithm)) {
                throw new CryptoServiceException("Unsupported algorithm: " + algorithm);
            }

            // Parse OpenPGP packets
            OpenPGPPacketParser parser = new OpenPGPPacketParser(encryptedData);
            OpenPGPPacket pkESKPacket = parser.readNextPacket();
            OpenPGPPacket sedPacket = parser.readNextPacket();

            if (pkESKPacket.getTag() != 1 || sedPacket.getTag() != 9) {
                throw new CryptoServiceException("Invalid OpenPGP packet structure");
            }

            // For demo purposes, we'll use symmetric decryption with provided key
            // In a full implementation, this would decrypt the session key first
            byte[] sessionKey = key.getBytes(StandardCharsets.UTF_8);

            // Extract IV and encrypted data from SED packet
            byte[] packetData = sedPacket.getData();
            if (packetData.length < 17) { // IV + at least 1 byte of data
                throw new CryptoServiceException("Invalid SED packet format");
            }

            byte[] iv = Arrays.copyOfRange(packetData, 0, 16);
            byte[] encryptedContent = Arrays.copyOfRange(packetData, 16, packetData.length);

            // Decrypt with AES-CFB
            SecretKeySpec keySpec = new SecretKeySpec(sessionKey, SESSION_KEY_ALGO);
            Cipher aesCipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            byte[] decryptedData = aesCipher.doFinal(encryptedContent);

            return DecryptionResult.builder()
                    .decryptedData(decryptedData)
                    .algorithm("OpenPGP-" + algorithm)
                    .build();

        } catch (Exception e) {
            throw new CryptoServiceException("OpenPGP decryption failed", e);
        }
    }

    @Override
    public boolean isAlgorithmSupported(String algorithm) {
        return "AES-256".equals(algorithm) || "OpenPGP-AES-256".equals(algorithm);
    }

    @Override
    public EncryptionOptions getDefaultOptions(String algorithm) {
        return EncryptionOptions.builder()
                .keyId("openpgp-default")
                .useCompression(false)
                .build();
    }

    @Override
    public void encryptFile(String inputFile, String outputFile, String algorithm, EncryptionOptions options) throws CryptoServiceException {
        try {
            byte[] inputData = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(inputFile));
            EncryptionResult result = encrypt(inputData, algorithm, options);

            // Add ASCII armor if requested
            byte[] outputData = result.getEncryptedData();
            if (options != null && options.isAsciiArmor()) {
                outputData = addAsciiArmor(outputData, "MESSAGE").getBytes(StandardCharsets.UTF_8);
            }

            java.nio.file.Files.write(java.nio.file.Paths.get(outputFile), outputData);
        } catch (Exception e) {
            throw new CryptoServiceException("OpenPGP file encryption failed", e);
        }
    }

    @Override
    public void decryptFile(String inputFile, String outputFile, String algorithm, String key) throws CryptoServiceException {
        try {
            byte[] inputData = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(inputFile));

            // Remove ASCII armor if present
            String inputString = new String(inputData, StandardCharsets.UTF_8);
            if (inputString.contains("-----BEGIN PGP MESSAGE-----")) {
                inputData = removeAsciiArmor(inputString).getBytes(StandardCharsets.UTF_8);
            }

            DecryptionResult result = decrypt(inputData, algorithm, key);
            java.nio.file.Files.write(java.nio.file.Paths.get(outputFile), result.getDecryptedData());
        } catch (Exception e) {
            throw new CryptoServiceException("OpenPGP file decryption failed", e);
        }
    }

    private byte[] createPKESKPacket(PublicKey publicKey, SecretKey sessionKey) throws Exception {
        // Public Key Encrypted Session Key Packet (Tag 1)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Version (3) + Key ID (8 bytes) + Public Key Algorithm (1 byte)
        baos.write(3); // Version
        baos.write(new byte[8]); // Key ID (placeholder)
        baos.write(1); // RSA

        // Encrypt session key with RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedSessionKey = rsaCipher.doFinal(sessionKey.getEncoded());

        baos.write(encryptedSessionKey.length >> 8);
        baos.write(encryptedSessionKey.length & 0xFF);
        baos.write(encryptedSessionKey);

        return createPacket((byte) 1, baos.toByteArray());
    }

    private byte[] createSEDPacket(SecretKey sessionKey, byte[] iv, byte[] encryptedData) throws Exception {
        // Symmetrically Encrypted Data Packet (Tag 9)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Encrypted data with IV prepended
        baos.write(iv);
        baos.write(encryptedData);

        return createPacket((byte) 9, baos.toByteArray());
    }

    private byte[] createPacket(byte tag, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // New format packet header
        if (data.length < 192) {
            baos.write(tag | 0xC0); // Tag + new format bit
            baos.write(data.length);
        } else if (data.length < 8384) {
            baos.write(tag | 0xC0);
            baos.write(((data.length - 192) / 256) + 192);
            baos.write((data.length - 192) % 256);
        } else {
            // Large packet - not implemented for this demo
            throw new IOException("Large packets not supported in demo");
        }

        baos.write(data);

        return baos.toByteArray();
    }

    private String addAsciiArmor(byte[] data, String type) {
        String b64Data = Base64.getEncoder().encodeToString(data);
        StringBuilder armored = new StringBuilder();

        armored.append("-----BEGIN PGP ").append(type).append("-----\n");
        armored.append("Version: OpenPGP.js v4.10.1\n");
        armored.append("\n");

        // Split base64 into 64-character lines
        for (int i = 0; i < b64Data.length(); i += 64) {
            armored.append(b64Data, i, Math.min(i + 64, b64Data.length())).append("\n");
        }

        armored.append("\n-----END PGP ").append(type).append("-----\n");

        return armored.toString();
    }

    private String removeAsciiArmor(String armored) throws CryptoServiceException {
        String[] lines = armored.split("\n");
        StringBuilder data = new StringBuilder();

        boolean inData = false;
        for (String line : lines) {
            if (line.startsWith("-----END PGP")) {
                break;
            }
            if (inData) {
                data.append(line);
            }
            if (line.startsWith("-----BEGIN PGP MESSAGE-----")) {
                inData = true;
            }
        }

        if (data.length() == 0) {
            throw new CryptoServiceException("No valid ASCII armor data found");
        }

        return data.toString();
    }
}

/**
 * Simple OpenPGP packet parser for demo purposes.
 */
class OpenPGPPacketParser {
    private final byte[] data;
    private int position = 0;

    public OpenPGPPacketParser(byte[] data) {
        this.data = data;
    }

    public OpenPGPPacket readNextPacket() throws IOException {
        if (position >= data.length) {
            return null;
        }

        int firstByte = data[position++] & 0xFF;
        byte tag = (byte) (firstByte & 0x3F); // Extract tag from new format header

        int length = readLength();
        byte[] packetData = Arrays.copyOfRange(data, position, position + length);
        position += length;

        return new OpenPGPPacket(tag, packetData);
    }

    private int readLength() throws IOException {
        int firstByte = data[position++] & 0xFF;

        if (firstByte < 192) {
            return firstByte;
        } else if (firstByte < 224) {
            int secondByte = data[position++] & 0xFF;
            return ((firstByte - 192) * 256) + secondByte + 192;
        } else {
            // Large packet - not implemented for this demo
            throw new IOException("Large packet lengths not supported in demo");
        }
    }
}

/**
 * Simple OpenPGP packet representation.
 */
class OpenPGPPacket {
    private final byte tag;
    private final byte[] data;

    public OpenPGPPacket(byte tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }

    public byte getTag() {
        return tag;
    }

    public byte[] getData() {
        return data;
    }
}
