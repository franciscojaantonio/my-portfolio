package pt.tecnico;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Program to read and write asymmetric key files
 */
public class AsymmetricKeys {

    /** Asymmetric cryptography algorithm and key size. */
    private static final String ASYM_ALGO = "RSA";
    private static final int ASYM_KEY_SIZE = 2048;
    private static boolean DEBUG = false;

    public static void SetDebug(boolean debug) {
        AsymmetricKeys.DEBUG = debug;
    }

    public static void write(String publicKeyPath, String privateKeyPath) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYM_ALGO);
        keyGen.initialize(ASYM_KEY_SIZE);
        KeyPair key = keyGen.generateKeyPair();


        byte[] pubEncoded = key.getPublic().getEncoded();
        writeFile(publicKeyPath, pubEncoded);

        byte[] privEncoded = key.getPrivate().getEncoded();
        writeFile(privateKeyPath, privEncoded);

        if (DEBUG) {
            System.out.println("Generating " + ASYM_ALGO + " keys ...");
            System.out.printf("%d bits%n", ASYM_KEY_SIZE);
            System.out.println("Public key info:");
            System.out.println("Algorithm: " + key.getPublic().getAlgorithm());
            System.out.println("Format: " + key.getPublic().getFormat());
            System.out.println("Writing public key to " + publicKeyPath + " ...");
            System.out.println("---");
            System.out.println("Private key info:");
            System.out.println("Algorithm: " + key.getPrivate().getAlgorithm());
            System.out.println("Format: " + key.getPrivate().getFormat());
            System.out.println("Writing private key to '" + privateKeyPath + "' ...");
        }
    }

    public static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        byte[] pubEncoded = readFile(publicKeyPath);

        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance(ASYM_ALGO);
        PublicKey pub = keyFacPub.generatePublic(pubSpec);

        if (DEBUG) {
            System.out.println("Reading public key from file " + publicKeyPath + " ...");
            System.out.println(pub);
        }

        return pub;
    }

    public static PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);

        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance(ASYM_ALGO);
        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);

        if (DEBUG) {
            System.out.println("Reading private key from file " + privateKeyPath + " ...");
        }

        return priv;
    }

    private static void writeFile(String path, byte[] content) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();
    }

    private static byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        
        return content;
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                write(args[0], args[1]);
            } 

            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}