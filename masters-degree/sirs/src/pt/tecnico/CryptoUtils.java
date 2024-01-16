package pt.tecnico;

import javax.crypto.*;

import java.io.FileNotFoundException;
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import com.google.gson.JsonObject;



public class CryptoUtils {

    private static boolean DEBUG = false;

    /** Signatures algorithm */
    private static final String SIGNATURE_ALGO = "SHA256withRSA";

    /** Asymmetric cypher */
    private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";

    /** Digest algorithm */
    private static final String DIGEST_ALGO = "SHA-256";

    /** Set with all the received Nonces to check freshness */
    private static final Set<byte[]> nonces = new HashSet<>();


    public static void SetDebug(boolean debug) {
        CryptoUtils.DEBUG = debug;
    }

    // Sign a JSON document
    protected static byte[] sign(PrivateKey privKey, JsonObject jsonObject) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGO);
            signature.initSign(privKey);

            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(jsonObject.toString().getBytes());
            byte[] jsonDigest = messageDigest.digest();

            signature.update(jsonDigest);

            byte[] signatureBytes = signature.sign();

            if (DEBUG) {
                System.out.println("Digest created: " + printHexBinary(jsonDigest) + " | Length: " + jsonDigest.length + "!");
                System.out.println("Signature created: " + printHexBinary(signatureBytes) + "!");
            }

            return signatureBytes;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("No such algorithm for signature"), e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(String.format("Invalid key for signature"), e);
        }
        catch (SignatureException e) {
            throw new RuntimeException(String.format("Signature creation failed"), e);
        }
    }

    private static String createTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").format(new Date());
    }

    private static byte[] createNonce() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        return bytes;
    }

    protected static JsonObject createFreshness(JsonObject jsonObj) {
        JsonObject metadata = new JsonObject();

        metadata.addProperty("nonce", encodeB64(createNonce()));
        metadata.addProperty("timestamp", createTimeStamp());

        jsonObj.add("metadata", metadata);

        return jsonObj;
    }

    // Encrypt a JSON document
    protected static byte[] encrypt(PublicKey publicKey, byte[] bytes) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            if (DEBUG) {
                System.out.println("Cipher created:");
                System.out.println(cipher.getAlgorithm() + "; " + cipher.getBlockSize());
            }

            return cipher.doFinal(bytes);
        }
        catch (NoSuchPaddingException | BadPaddingException e) {
            throw new RuntimeException(String.format("Padding does not exist or not padded properly."), e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("No such algorithm for signature"), e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(String.format("Invalid key for signature"), e);
        }
    }

    // Decrypt a JSON document
    protected static byte[] decrypt(PrivateKey privateKey, byte[] bytes) throws NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance(ASYM_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            if (DEBUG) {
                System.out.println("Cipher created:");
                System.out.println(cipher.getAlgorithm() + "; " + cipher.getBlockSize());
            }

            return cipher.doFinal(bytes);
        }
        catch (NoSuchPaddingException | BadPaddingException e) {
            throw new RuntimeException(String.format("Padding does not exist or not padded properly."), e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("No such algorithm for signature"), e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(String.format("Invalid key for signature"), e);
        }
    }

    // Check the freshness of a JSON document
    protected static boolean checkFreshness(JsonObject jsonObj) throws ParseException {
        try {
            JsonObject metadata = jsonObj.getAsJsonObject("metadata");

            String timeStamp = metadata.get("timestamp").getAsString();
            String nonce = metadata.get("nonce").getAsString();

            // Check the timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy");
            Date date = dateFormat.parse(timeStamp);
            Date now = new Date();

            long diff = now.getTime() - date.getTime();
            long diffMinutes = diff / (60 * 1000);

            if (diffMinutes > 43800) {
                // TODO meti isto pq acho que este print devia acontecer sempre.
                /* if (DEBUG) {
                    System.out.println("The document is not fresh! (Timestamp)");
                } */
                System.out.println("The document is not fresh! (Timestamp)");

                return false;
            }

            // Check the nonce
            if (!nonces.add(decodeB64(nonce))) {
                // TODO meti isto pq acho que este print devia acontecer sempre.
                System.out.println("The document is not fresh! (Nonce)");

                return false;
            }

            return true;
        }
        catch (ParseException e) {
            throw new RuntimeException(String.format("Could not parse time stamp"), e);
        }
    }

    // Check the integrity of a JSON document
    protected static boolean checkIntegrity(PublicKey publicKey, JsonObject jsonObj, String signature) throws IllegalArgumentException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGO);
            sig.initVerify(publicKey);

            MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(jsonObj.toString().getBytes());
            byte[] jsonDigest = messageDigest.digest();

            sig.update(jsonDigest);

            boolean isValid = sig.verify(decodeB64(signature));

            if (DEBUG) {
                System.out.println("Digest created: " + printHexBinary(jsonDigest) + " | Length: " + jsonDigest.length + "!");
                System.out.println("Signature verification: " + printHexBinary(decodeB64(signature)) + "!");
            }

            return isValid;
    
        }
        catch (IllegalArgumentException | SignatureException e) {
            throw new RuntimeException(String.format("Signature verification failed"), e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("No such algorithm for signature"), e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(String.format("Invalid key for signature"), e);
        }
    }
    
    static String encodeB64(byte[] bytes) throws IllegalArgumentException {
        try {
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (IllegalArgumentException e) {
            // Handle the exception or rethrow it as needed
            throw new RuntimeException("Error encoding to Base64", e);
        }
    }

    static byte[] decodeB64(String str) throws IllegalArgumentException {
        try{ 
            return Base64.getDecoder().decode(str);
        }    
        catch (IllegalArgumentException e) {
            // Handle the exception or rethrow it as needed
            throw new RuntimeException("Error decoding Base64", e);
        }
    }
}