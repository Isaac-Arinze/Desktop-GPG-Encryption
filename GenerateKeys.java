import com.cryptomanager.service.impl.SimpleKeyManagementService;
import com.cryptomanager.service.model.KeyMetadata;
import com.cryptomanager.service.exception.CryptoServiceException;

public class GenerateKeys {
    public static void main(String[] args) {
        try {
            System.out.println("🔑 === CRYPTO MANAGER - KEY GENERATION TEST ===\n");

            SimpleKeyManagementService keyManagementService = new SimpleKeyManagementService();

            // Generate key pairs for testing
            String aliceKeyId = "alice-key";
            String bobKeyId = "bob-key";
            String passphrase = "MySecretPassphrase123";

            System.out.println("📁 Key storage location:");
            System.out.println("C:\\Users\\Staff\\.crypto-manager\\keys\\\n");

            // Generate Alice's key pair
            System.out.println("🔨 Generating Alice's RSA key pair...");
            KeyMetadata aliceKey = keyManagementService.generateKeyPair(aliceKeyId);
            System.out.println("✅ Alice's key generated: " + aliceKey.getKeyId());
            System.out.println("   Algorithm: " + aliceKey.getAlgorithm());
            System.out.println("   Key Size: " + aliceKey.getKeySize() + " bits");
            System.out.println("   Created: " + new java.util.Date(aliceKey.getCreatedAt()));
            System.out.println();

            // Generate Bob's key pair
            System.out.println("🔨 Generating Bob's RSA key pair...");
            KeyMetadata bobKey = keyManagementService.generateKeyPair(bobKeyId);
            System.out.println("✅ Bob's key generated: " + bobKey.getKeyId());
            System.out.println("   Algorithm: " + bobKey.getAlgorithm());
            System.out.println("   Key Size: " + bobKey.getKeySize() + " bits");
            System.out.println("   Created: " + new java.util.Date(bobKey.getCreatedAt()));
            System.out.println();

            // List all keys
            System.out.println("📋 Listing all generated keys:");
            KeyMetadata[] keys = keyManagementService.listKeys();
            for (KeyMetadata key : keys) {
                System.out.println("  🔑 " + key.getKeyId() + " (" + key.getAlgorithm() + " " + key.getKeySize() + "bit)");
            }
            System.out.println();

            // Export public keys
            System.out.println("📤 Exporting public keys:");
            String alicePublicKey = keyManagementService.exportPublicKey(aliceKeyId);
            String bobPublicKey = keyManagementService.exportPublicKey(bobKeyId);

            System.out.println("Alice's Public Key (first 50 chars): " +
                             alicePublicKey.substring(0, Math.min(50, alicePublicKey.length())) + "...");
            System.out.println("Bob's Public Key (first 50 chars): " +
                             bobPublicKey.substring(0, Math.min(50, bobPublicKey.length())) + "...");
            System.out.println();

            System.out.println("🎯 SUCCESS! Keys generated and ready for use.");
            System.out.println("📁 Check C:\\Users\\Staff\\.crypto-manager\\keys\\ for key files");

        } catch (CryptoServiceException e) {
            System.err.println("❌ Key generation failed: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
