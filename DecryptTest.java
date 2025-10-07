import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import com.cryptomanager.openpgp.OpenPGPEncryptionService;
import com.cryptomanager.service.exception.CryptoServiceException;
import com.cryptomanager.service.model.DecryptionResult;

public class DecryptTest {
    public static void main(String[] args) {
        try {
            System.out.println("🔓 === CRYPTO MANAGER DESKTOP - DECRYPTION TEST ===\n");

            // Initialize the OpenPGP encryption service
            OpenPGPEncryptionService decryptionService = new OpenPGPEncryptionService();

            // Read the encrypted file
            String encryptedFilePath = "encrypted-message.txt.asc";
            System.out.println("📁 Reading encrypted file: " + encryptedFilePath);

            byte[] encryptedData = Files.readAllBytes(Paths.get(encryptedFilePath));
            String encryptedContent = new String(encryptedData, StandardCharsets.UTF_8);

            System.out.println("📄 Encrypted file content:");
            System.out.println(encryptedContent);
            System.out.println();

            // Remove ASCII armor to get raw encrypted data
            String base64Data = removeAsciiArmor(encryptedContent);
            byte[] rawEncryptedData = Base64.getDecoder().decode(base64Data);

            System.out.println("🔑 Enter the decryption key/passphrase used during encryption:");
            System.out.println("   (This should be the same key you used to encrypt the message)");

            // For demo purposes, try common passphrases
            String[] possibleKeys = {
                "password",
                "secret",
                "test",
                "demo",
                "MySecretPassphrase123",
                "default-passphrase"
            };

            boolean decrypted = false;
            for (String key : possibleKeys) {
                try {
                    System.out.println("🔄 Trying key: " + key);

                    DecryptionResult decryptionResult = decryptionService.decrypt(
                        rawEncryptedData,
                        "AES-256",
                        key
                    );

                    String decryptedMessage = new String(decryptionResult.getDecryptedData(), StandardCharsets.UTF_8);
                    System.out.println("\n✅ DECRYPTION SUCCESSFUL!");
                    System.out.println("🔓 Decrypted message: " + decryptedMessage);
                    System.out.println("🔑 Used key: " + key);
                    decrypted = true;
                    break;

                } catch (CryptoServiceException e) {
                    System.out.println("❌ Failed with key '" + key + "': " + e.getMessage());
                }
            }

            if (!decrypted) {
                System.out.println("\n❌ Could not decrypt with common keys.");
                System.out.println("💡 Please provide the correct decryption key/passphrase.");
                System.out.println("\n📝 MANUAL DECRYPTION:");
                System.out.println("If you know the correct key, you can modify this program:");
                System.out.println("1. Replace 'your-key-here' in the decrypt() call below");
                System.out.println("2. Run the program again");
                System.out.println();
                System.out.println("DecryptionResult decryptionResult = decryptionService.decrypt(");
                System.out.println("    rawEncryptedData,");
                System.out.println("    \"AES-256\",");
                System.out.println("    \"your-key-here\"  // ← Replace this with your actual key");
                System.out.println(");");
            }

            // Show how to use the desktop application
            System.out.println("\n🖥️  USING THE DESKTOP APPLICATION:");
            System.out.println("1. Open your Crypto Manager Desktop application");
            System.out.println("2. Click 'Decrypt File' or similar option");
            System.out.println("3. Select 'Decrypt File (Symmetric)'");
            System.out.println("4. Enter the path to your encrypted file:");
            System.out.println("   " + encryptedFilePath);
            System.out.println("5. Enter the same key/passphrase you used for encryption");
            System.out.println("6. Click 'OK' to decrypt");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String removeAsciiArmor(String armored) throws CryptoServiceException {
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
