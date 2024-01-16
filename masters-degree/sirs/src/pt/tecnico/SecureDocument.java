package pt.tecnico;

import com.google.gson.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;



public class SecureDocument {
    private static boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        args = CleanArgs(args);

        if (args.length < 1 || args[0].equals("--help") || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--h")) {
            PrintUsage();
        }

        else if (args[0].equals("protect")) {
            if (args[1].equals("--voucher") || args[1].equals("-v") || args[1].equals("-voucher") || args[1].equals("--v")) {

                if (args.length < 6) {
                    PrintUsage();
                    return;
                }

                JsonObject outputJson = new JsonObject();

                if (args[2].equals("-t")) { // protect a voucher transfer
                    final String newOwnerEncryptionKey = args[3];
                    final String previousOwnerSigningKey = args[4];
                    final String inputFileOriginal = args[5];
                    final String inputFileUnprotected = args[6];
                    final String outputFile = args[7];

                    JsonObject voucherJson = Utils.ExtractJson(inputFileUnprotected);
                    JsonObject voucherJsonOriginal = Utils.ExtractJson(inputFileOriginal);

                    byte[] encryptedCode = protectConfidentiality(voucherJson.getAsJsonObject("mealVoucher"), newOwnerEncryptionKey);

                    voucherJson.addProperty("mealVoucher", CryptoUtils.encodeB64(encryptedCode));

                    outputJson.add("data", voucherJson);

                    outputJson = protectAuthenticity(outputJson, previousOwnerSigningKey, voucherJsonOriginal.getAsJsonObject("metadata").get("voucherSignature").getAsString());

                    PrintBeforeAndAfter(voucherJsonOriginal, voucherJson, outputJson);
                    Utils.WriteJsonFile(outputFile, outputJson);
                }
                else { // protect a voucher creation
                    final String voucherSigningKey = args[2];
                    final String ownerEncryptionKey = args[3];
                    final String inputFile = args[4];
                    final String outputFile = args[5];

                    JsonObject voucherJson = Utils.ExtractJson(inputFile);
                    String voucherSignature = getSignature(voucherJson.getAsJsonObject("mealVoucher"), voucherSigningKey);

                    byte[] encryptedCode = protectConfidentiality(voucherJson.getAsJsonObject("mealVoucher"), ownerEncryptionKey);

                    voucherJson.addProperty("mealVoucher", CryptoUtils.encodeB64(encryptedCode));

                    outputJson.add("data", voucherJson);

                    outputJson = protectAuthenticity(outputJson, voucherSigningKey, voucherSignature);

                    Utils.WriteJsonFile(outputFile, outputJson);
                }

            } else if (args[1].equals("--info") || args[1].equals("-i") || args[1].equals("-info") || args[1].equals("--i")) {

                if (args.length != 5) {
                    PrintUsage();
                    return;
                }

                JsonObject outputJson = new JsonObject();

                final String SigningKeyPath = args[2];
                final String inputFile = args[3];
                final String outputFile = args[4];

                JsonObject jsonToProtect = Utils.ExtractJson(inputFile);

                outputJson.add("data", jsonToProtect);

                outputJson = protectAuthenticity(outputJson, SigningKeyPath, null);

                System.out.println(outputJson);

                Utils.WriteJsonFile(outputFile, outputJson);

            } else if (args[1].equals("--review") || args[1].equals("-r") || args[1].equals("-review") || args[1].equals("--r")) {

                if (args.length != 5) {
                    PrintUsage();
                    return;
                }

                JsonObject outputJson = new JsonObject();

                final String SigningKeyPath = args[2];
                final String inputFile = args[3];
                final String outputFile = args[4];

                JsonObject jsonToProtect = Utils.ExtractJson(inputFile);

                outputJson.add("data", jsonToProtect);

                outputJson = protectAuthenticity(outputJson, SigningKeyPath, null);

                System.out.println(outputJson);

                PrintBeforeAndAfter(jsonToProtect, outputJson);
                Utils.WriteJsonFile(outputFile, outputJson);
            }
        }

        else if (args[0].equals("unprotect")) {
            if (args.length < 3) {
                PrintUsage();
                return;
            }

            JsonObject outputJson;

            if (args[1].equals("-e")) { // if e flag is selected, ie, to decrypt, it can only be a voucher

                final String decrytionKeyPath = args[2];
                final String inputFile = args[3];
                final String outputFile = args[4];

                JsonElement jsonToUnprotect = Utils.ExtractJson(inputFile).get("data");
                JsonObject before = Utils.ExtractJson(inputFile);
                outputJson = jsonToUnprotect.getAsJsonObject();
                JsonObject mealVoucherDecrypted = unprotect(CryptoUtils.decodeB64(outputJson.getAsJsonPrimitive("mealVoucher").getAsString()), decrytionKeyPath);

                outputJson = jsonToUnprotect.getAsJsonObject();
                outputJson.add("mealVoucher", mealVoucherDecrypted);

                PrintBeforeAndAfter(before, outputJson);
                Utils.WriteJsonFile(outputFile, outputJson);

            } else { // neither reviews or restaurant info require decryption

                final String inputFile = args[1];
                final String outputFile = args[2];

                JsonElement jsonToUnprotect = Utils.ExtractJson(inputFile).get("data");
                JsonObject before = Utils.ExtractJson(inputFile);
                outputJson = jsonToUnprotect.getAsJsonObject();

                PrintBeforeAndAfter(before, outputJson);
                Utils.WriteJsonFile(outputFile, outputJson);
            }
        }

        else if (args[0].equals("check")) {
            if (args.length != 3) {
                PrintUsage();
                return;
            }
            final String signatureKey = args[1];
            final String inputFile = args[2];

            JsonObject jsonToCheck = Utils.ExtractJson(inputFile);
            check(jsonToCheck, signatureKey);
        }
    }

    public static String[] CleanArgs(String [] args) {
        List<String> cpyArgs = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d") || args[i].equals("--debug") || args[i].equals("-debug") || args[i].equals("--d")) {
                DEBUG = true;
                Utils.SetDebug(DEBUG);
                CryptoUtils.SetDebug(DEBUG);
                AsymmetricKeys.SetDebug(DEBUG);
                
                continue;
            }

            cpyArgs.add(args[i]);
        }

        return cpyArgs.toArray(new String[0]);
    }

    // Protect a JSON document with authenticity
    public static JsonObject protectAuthenticity(JsonObject jsonObj, String privateKeyPath, String extraSignature) throws Exception {
        // Read the private key
        PrivateKey privateKey = AsymmetricKeys.readPrivateKey(privateKeyPath);

        // Add freshness to the document
        JsonObject freshJsonObj = CryptoUtils.createFreshness(jsonObj);

        if (extraSignature != null) {
            freshJsonObj.getAsJsonObject("metadata").addProperty("voucherSignature", extraSignature);
        }

        byte[] signature = CryptoUtils.sign(privateKey, freshJsonObj);

        // Add the signature to the fresh document
        freshJsonObj.addProperty("signature", CryptoUtils.encodeB64(signature));

        // Print the protected document
        if (DEBUG) {
            System.out.println("Document with Authenticity:");
            System.out.println(freshJsonObj);
        }
        
        return freshJsonObj;
    }

    public static String getSignature(JsonObject jsonObj, String privateKeyPath) throws Exception {
        // Read the private key
        PrivateKey privateKey = AsymmetricKeys.readPrivateKey(privateKeyPath);

        byte[] signature = CryptoUtils.sign(privateKey, jsonObj);

        // Print the signature
        if (DEBUG) {
            System.out.println("Document with Authenticity:");
            System.out.println(CryptoUtils.encodeB64(signature));
        }

        return CryptoUtils.encodeB64(signature);
    }

    // Protect a JSON document with confidentiality
    public static byte[] protectConfidentiality(JsonObject jsonObj, String destPublicKeyPath) throws Exception {
        // Read the public key
        PublicKey publicKey = AsymmetricKeys.readPublicKey(destPublicKeyPath);

        // Encrypt the document
        byte[] encrypted = CryptoUtils.encrypt(publicKey, jsonObj.toString().getBytes());

        // Print the encrypted document
        if (DEBUG) {
            System.out.println("Encrypted document:");
            System.out.println(CryptoUtils.encodeB64(encrypted));
        }
        
        return encrypted;
    }

    // Check if a JSON document has authenticity (its integrity and freshness)
    public static boolean check(JsonObject jsonObj, String srcPublicKeyPath) throws Exception {
        // Read the public key
        PublicKey publicKey = AsymmetricKeys.readPublicKey(srcPublicKeyPath);

        // Get the signature
        String signature = jsonObj.get("signature").getAsString();

        // Remove the signature from the document
        jsonObj.remove("signature");

        // Check the freshness
        if (!CryptoUtils.checkFreshness(jsonObj)) {
            System.out.println("The document is not fresh!");
            
            return false;
        }

        System.out.println("The document is fresh!");

        // Check the integrity
        if (!CryptoUtils.checkIntegrity(publicKey, jsonObj, signature)) {
            System.out.println("The document has been modified!");
            return false;
        }

        System.out.printf("The document has not been modified!%n");

        return true;
    }

    // Unprotect a JSON document with confidentiality
    public static JsonObject unprotect(byte[] encrypted, String privateKeyPath) throws Exception {
        // Read the private key
        PrivateKey privateKey = AsymmetricKeys.readPrivateKey(privateKeyPath);

        // Decrypt the document
        byte[] decrypted = CryptoUtils.decrypt(privateKey, encrypted);
        String strDecrypted = new String(decrypted);

        JsonObject unprotectedObject = JsonParser.parseString(strDecrypted).getAsJsonObject();
        // Print the decrypted document
        System.out.println("Decrypted document:");
        System.out.println(strDecrypted);

        return unprotectedObject;
    }

    private static void PrintBeforeAndAfter(JsonObject before, JsonObject after) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(before.toString());
        String prettyJsonString = gson.toJson(je);

        System.err.println("------------------------------------------------------\nBefore:\n" + prettyJsonString);

        je = JsonParser.parseString(after.toString());
        prettyJsonString = gson.toJson(je);
        System.err.println("\nAfter:\n" + prettyJsonString + "\n------------------------------------------------------\n");
    }

    private static void PrintBeforeAndAfter(JsonObject original, JsonObject before, JsonObject after) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(original.toString());
        String prettyJsonString = gson.toJson(je);

        System.err.println("------------------------------------------------------\nOriginal:\n" + prettyJsonString);

        je = JsonParser.parseString(before.toString());
        prettyJsonString = gson.toJson(je);

        System.err.println("\nBefore:\n" + prettyJsonString + "\n------------------------------------------------------\n");


        je = JsonParser.parseString(after.toString());
        prettyJsonString = gson.toJson(je);

        System.err.println("\nAfter:\n" + prettyJsonString + "\n------------------------------------------------------\n");
    }

    private static void PrintUsage() {
        System.out.println("NAME");
        System.out.println("    secdoc - protect, check and unprotect Json files.");
        System.out.println("SYNOPSIS");
        System.out.println("    secdoc protect [OPTIONS] <input-file> <output-file>");
        System.out.println("        secdoc protect --info <path-to-signing-key> <input-file> <output-file>");
        System.out.println("        secdoc protect --review <path-to-signing-key> <input-file> <output-file>");
        System.out.println("        to protect a voucher there are two options: when creating and when transferring (-t):");
        System.out.println("            secdoc protect --voucher <path-to-signing-key> <path-to-encryption-key> <input-file> <output-file>");
        System.out.println("            secdoc protect --voucher -t <path-to-new-owner-encryption-key> <path-to-signing-key> <original-protected-input-file> <unprotected-input-file> <output-file>");
        System.out.println("    secdoc check <path-to-signature-key> <input-file>");
        System.out.println("    secdoc unprotect [OPTIONS] <input-file> <output-file>");
        System.out.println("        to unprotect a voucher, which requires an aditional key:");
        System.out.println("        secdoc unprotect -e <path-to-decrytion-key> <input-file> <output-file>");
        System.out.println("DESCRIPTION");
        System.out.println("    The secdoc command is a utility for securing and managing sensitive documents.");
        System.out.println("OPTIONS");
        System.out.println("    -h, --help");
        System.out.println("        Display this help message.");
        System.out.println("    -d, --debug");
        System.out.println("        Explain what is being checked.");
        System.out.println("    protect");
        System.out.println("        -i, --info");
        System.out.println("            Protect a restaurant info, in Json format, with Asymmetric Key.");
        System.out.println("        -v, --voucher");
        System.out.println("            Protect a voucher, in Json format, with Asymmetric Key.");
        System.out.println("    check");
        System.out.println("        --empty-option");
        System.out.println("            Check if file has been tampered with, and it's freshness.");
        System.out.println("    unprotect");
        System.out.println("        --empty-option");
        System.out.println("            Unprotect a file.");
        System.out.println("        --e");
        System.out.println("            Unprotect a voucher with Asymmetric Key.");
        System.out.println("AUTHORS");
        System.out.println("    Written by: Duarte Jeremias, Francisco António, Rodrigo Liu");
    }
}
