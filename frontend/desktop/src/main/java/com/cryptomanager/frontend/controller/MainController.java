package com.cryptomanager.frontend.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import com.cryptomanager.service.EncryptionService;
import com.cryptomanager.service.KeyManagementService;
import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.impl.SimpleEncryptionService;
import com.cryptomanager.service.impl.SimpleKeyManagementService;
import com.cryptomanager.service.model.DecryptionResult;
import com.cryptomanager.service.model.EncryptionOptions;
import com.cryptomanager.service.model.EncryptionResult;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;

public class MainController {

    @FXML
    private TabPane mainTabs;

    private final EncryptionService encryptionService = new SimpleEncryptionService();
    private final KeyManagementService keyManagementService = new SimpleKeyManagementService();

    @FXML
    public void initialize() {}

    @FXML
    private void onExit(ActionEvent e) {
        System.exit(0);
    }

    @FXML
    private void onOpenEncrypt(ActionEvent e) {
        // Get text to encrypt
        TextInputDialog textDialog = new TextInputDialog();
        textDialog.setTitle("Encrypt Text (Symmetric Encryption)");
        textDialog.setHeaderText("Enter text to encrypt");
        Optional<String> textOpt = textDialog.showAndWait();
        if (textOpt.isEmpty()) return;

        // Get encryption passphrase (symmetric encryption for now)
        TextInputDialog passphraseDialog = new TextInputDialog();
        passphraseDialog.setTitle("Encryption Passphrase");
        passphraseDialog.setHeaderText("Enter passphrase for symmetric encryption");
        passphraseDialog.setContentText("Passphrase:");
        Optional<String> passphraseOpt = passphraseDialog.showAndWait();
        if (passphraseOpt.isEmpty()) return;

        try {
            String passphrase = passphraseOpt.get().trim();
            if (passphrase.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Invalid Passphrase");
                a.setHeaderText("Passphrase cannot be empty");
                a.showAndWait();
                return;
            }

            // Use the encryption service with passphrase as key ID
            EncryptionOptions opts = EncryptionOptions.builder()
                    .keyId(passphrase)
                    .asciiArmor(true)
                    .build();

            EncryptionResult result = encryptionService.encrypt(
                textOpt.get().getBytes(StandardCharsets.UTF_8),
                "AES-GCM",
                opts
            );

            // Save to file
            TextInputDialog fileDialog = new TextInputDialog("encrypted-message.txt.asc");
            fileDialog.setTitle("Save Encrypted File");
            fileDialog.setHeaderText("Enter filename for encrypted output");
            fileDialog.setContentText("Filename:");
            Optional<String> fileOpt = fileDialog.showAndWait();

            if (fileOpt.isPresent() && !fileOpt.get().trim().isEmpty()) {
                String filename = fileOpt.get().trim();
                if (!filename.endsWith(".asc") && !filename.endsWith(".gpg")) {
                    filename += ".asc";
                }

                // Add ASCII armor for better compatibility
                String output = new String(result.getEncryptedData(), StandardCharsets.UTF_8);
                if (!output.contains("-----BEGIN PGP MESSAGE-----")) {
                    output = "-----BEGIN PGP MESSAGE-----\n" +
                            "Version: CryptoManager v0.1.0\n" +
                            "\n" +
                            Base64.getEncoder().encodeToString(result.getEncryptedData()).replaceAll("(.{64})", "$1\n") +
                            "\n-----END PGP MESSAGE-----\n";
                }

                java.nio.file.Files.write(java.nio.file.Paths.get(filename), output.getBytes(StandardCharsets.UTF_8));

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Symmetric Encryption Complete");
                successAlert.setHeaderText("File encrypted successfully!");
                successAlert.setContentText("File saved as: " + filename +
                    "\n\n✅ Encrypted with passphrase" +
                    "\n🔓 Use the same passphrase to decrypt" +
                    "\n📧 Share this file with the recipient");
                successAlert.showAndWait();
            } else {
                // Show in dialog if no file save
                String output = new String(result.getEncryptedData(), StandardCharsets.UTF_8);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Symmetric Encrypted Output");
                a.setHeaderText("OpenPGP Message (Symmetric Encrypted)");
                a.getDialogPane().setContentText(output);
                a.showAndWait();
            }
        } catch (CryptoServiceException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Encryption Error");
            a.setHeaderText("Symmetric encryption failed");
            a.setContentText(ex.getMessage() + "\n\nPlease check your passphrase and try again.");
            a.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("File Save Error");
            a.setHeaderText("Failed to save encrypted file");
            a.setContentText(ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void onOpenDecrypt(ActionEvent e) {
        // Get encrypted file
        TextInputDialog fileDialog = new TextInputDialog();
        fileDialog.setTitle("Decrypt File (Symmetric)");
        fileDialog.setHeaderText("Enter path to encrypted file (.asc or .gpg)");
        fileDialog.setContentText("File path:");
        Optional<String> fileOpt = fileDialog.showAndWait();
        if (fileOpt.isEmpty()) return;

        // Get decryption passphrase
        TextInputDialog passphraseDialog = new TextInputDialog();
        passphraseDialog.setTitle("Decryption Passphrase");
        passphraseDialog.setHeaderText("Enter the passphrase used for encryption");
        passphraseDialog.setContentText("Passphrase:");
        Optional<String> passphraseOpt = passphraseDialog.showAndWait();
        if (passphraseOpt.isEmpty()) return;

        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(fileOpt.get());
            if (!java.nio.file.Files.exists(filePath)) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("File Not Found");
                a.setHeaderText("Encrypted file not found");
                a.setContentText("Please check the file path and try again.");
                a.showAndWait();
                return;
            }

            String passphrase = passphraseOpt.get().trim();
            if (passphrase.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Invalid Passphrase");
                a.setHeaderText("Passphrase cannot be empty");
                a.showAndWait();
                return;
            }

            String fileContent = new String(java.nio.file.Files.readAllBytes(filePath), StandardCharsets.UTF_8);

            byte[] encryptedData;
            // Check if it's ASCII armored (contains PGP headers)
            if (fileContent.contains("-----BEGIN PGP MESSAGE-----")) {
                // Extract and decode Base64 content from ASCII armor
                String base64Content = fileContent
                    .replace("-----BEGIN PGP MESSAGE-----", "")
                    .replace("-----END PGP MESSAGE-----", "")
                    .replace("Version: CryptoManager v0.1.0", "")
                    .replaceAll("\\s", ""); // Remove all whitespace

                encryptedData = Base64.getDecoder().decode(base64Content);
            } else {
                // Assume it's raw binary data
                encryptedData = fileContent.getBytes(StandardCharsets.UTF_8);
            }

            // Use the same passphrase for decryption
            DecryptionResult result = ((com.cryptomanager.service.impl.SimpleEncryptionService) encryptionService)
                    .decrypt(encryptedData, "AES-GCM", passphrase);

            // Save decrypted content
            TextInputDialog outputDialog = new TextInputDialog("decrypted-output.txt");
            outputDialog.setTitle("Save Decrypted File");
            outputDialog.setHeaderText("Enter filename for decrypted output");
            outputDialog.setContentText("Filename:");
            Optional<String> outputOpt = outputDialog.showAndWait();

            if (outputOpt.isPresent() && !outputOpt.get().trim().isEmpty()) {
                String outputFilename = outputOpt.get().trim();
                java.nio.file.Files.write(java.nio.file.Paths.get(outputFilename), result.getDecryptedData());

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Symmetric Decryption Complete");
                successAlert.setHeaderText("File decrypted successfully!");
                successAlert.setContentText("✅ Decrypted with passphrase" +
                    "\n📄 Content saved as: " + outputFilename +
                    "\n🔓 Used the same passphrase as for encryption");
                successAlert.showAndWait();
            } else {
                // Show in dialog
                String decryptedText = new String(result.getDecryptedData(), StandardCharsets.UTF_8);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Decrypted Content");
                a.setHeaderText("Symmetrically Decrypted Text");
                a.setContentText(decryptedText);
                a.showAndWait();
            }

        } catch (CryptoServiceException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Decryption Error");
            a.setHeaderText("Failed to decrypt file");
            a.setContentText("Error: " + ex.getMessage() + "\n\nPlease check your passphrase and file format.");
            a.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("File Error");
            a.setHeaderText("Failed to read encrypted file");
            a.setContentText(ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void onAbout(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("About Crypto Manager");
        a.setHeaderText("Crypto Manager v0.1.0");
        a.setContentText("A desktop application for encryption, signing, and checksums.\n\nCreated with JavaFX and Java 21.");
        a.showAndWait();
    }

    @FXML
    private void onImportKeys(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Import Keys");
        a.setContentText("Key import functionality coming soon!");
        a.showAndWait();
    }

    @FXML
    private void onExportKeys(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Export Keys");
        a.setContentText("Key export functionality coming soon!");
        a.showAndWait();
    }

    @FXML
    private void onOpenSign(ActionEvent e) {}

    @FXML
    private void onOpenChecksum(ActionEvent e) {}

    @FXML
    private void onGenerateKey(ActionEvent e) {}

    @FXML
    private void onDeleteKey(ActionEvent e) {}
}
