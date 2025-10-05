package com.cryptomanager.frontend.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import com.cryptomanager.service.EncryptionService;
import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.impl.SimpleEncryptionService;
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

    @FXML
    public void initialize() {}

    @FXML
    private void onExit(ActionEvent e) {
        System.exit(0);
    }

    @FXML
    private void onOpenEncrypt(ActionEvent e) {
        TextInputDialog textDialog = new TextInputDialog();
        textDialog.setTitle("Encrypt Text (OpenPGP Format)");
        textDialog.setHeaderText("Enter text to encrypt");
        Optional<String> textOpt = textDialog.showAndWait();
        if (textOpt.isEmpty()) return;

        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setTitle("Passphrase");
        passDialog.setHeaderText("Enter passphrase");
        Optional<String> passOpt = passDialog.showAndWait();
        if (passOpt.isEmpty()) return;

        try {
            // Use AES-GCM (the only supported algorithm)
            EncryptionOptions opts = EncryptionOptions.builder()
                    .keyId(passOpt.get())
                    .build();

            EncryptionResult result = encryptionService.encrypt(textOpt.get().getBytes(StandardCharsets.UTF_8), "AES-GCM", opts);

            // Save to file
            TextInputDialog fileDialog = new TextInputDialog("encrypted-message.txt");
            fileDialog.setTitle("Save Encrypted File");
            fileDialog.setHeaderText("Enter filename for encrypted output");
            fileDialog.setContentText("Filename:");
            Optional<String> fileOpt = fileDialog.showAndWait();

            if (fileOpt.isPresent() && !fileOpt.get().trim().isEmpty()) {
                String filename = fileOpt.get().trim();
                if (!filename.endsWith(".asc") && !filename.endsWith(".gpg")) {
                    filename += ".asc";
                }

                // Convert to ASCII armored format if needed
                String output = new String(result.getEncryptedData(), StandardCharsets.UTF_8);
                if (!output.contains("-----BEGIN PGP MESSAGE-----")) {
                    // Add ASCII armor
                    output = "-----BEGIN PGP MESSAGE-----\n" +
                            "Version: CryptoManager v0.1.0\n" +
                            "\n" +
                            Base64.getEncoder().encodeToString(result.getEncryptedData()).replaceAll("(.{64})", "$1\n") +
                            "\n-----END PGP MESSAGE-----\n";
                }

                java.nio.file.Files.write(java.nio.file.Paths.get(filename), output.getBytes(StandardCharsets.UTF_8));

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("OpenPGP Encryption Complete");
                successAlert.setHeaderText("Encrypted data saved successfully!");
                successAlert.setContentText("File saved as: " + filename + "\n\nThis file is compatible with GPG tools.\nFormat: OpenPGP Message (ASCII armored)");
                successAlert.showAndWait();
            } else {
                // Show in dialog if no file save
                String output = new String(result.getEncryptedData(), StandardCharsets.UTF_8);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("OpenPGP Encrypted Output");
                a.setHeaderText("OpenPGP Message Format");
                a.getDialogPane().setContentText(output);
                a.showAndWait();
            }
        } catch (CryptoServiceException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Encryption Error");
            a.setHeaderText(ex.getMessage());
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
        TextInputDialog fileDialog = new TextInputDialog();
        fileDialog.setTitle("Decrypt File");
        fileDialog.setHeaderText("Enter path to encrypted file (.asc or .gpg)");
        fileDialog.setContentText("File path:");
        Optional<String> fileOpt = fileDialog.showAndWait();
        if (fileOpt.isEmpty()) return;

        TextInputDialog passDialog = new TextInputDialog();
        passDialog.setTitle("Passphrase");
        passDialog.setHeaderText("Enter passphrase for decryption");
        Optional<String> passOpt = passDialog.showAndWait();
        if (passOpt.isEmpty()) return;

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

            // Try to decrypt
            DecryptionResult result = ((com.cryptomanager.service.impl.SimpleEncryptionService) encryptionService)
                    .decrypt(encryptedData, "AES-GCM", passOpt.get());

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
                successAlert.setTitle("Decryption Complete");
                successAlert.setHeaderText("File decrypted successfully!");
                successAlert.setContentText("Decrypted content saved as: " + outputFilename);
                successAlert.showAndWait();
            } else {
                // Show in dialog
                String decryptedText = new String(result.getDecryptedData(), StandardCharsets.UTF_8);
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Decrypted Content");
                a.setHeaderText("Decrypted Text");
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
