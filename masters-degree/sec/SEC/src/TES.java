package src;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;

public class TES {

    // Accounts
    private Hashtable<PublicKey, Integer> accounts;

    // Initial balance value
    private final int INITIAL_BALANCE = 100;
    
    //Transfer fee
    private final int GAS_FEE = 10;

    private boolean isLeaderInit;

    public TES() {
        accounts = new Hashtable<>();
        isLeaderInit = false;
    }
    
    public boolean isValidCreateAccount(String strPublicKey) {

        PublicKey publicKey = getPublicKey(strPublicKey);
        
        if (accounts.containsKey(publicKey)) {
            System.err.println("Sorry the account: " + strPublicKey + "already exists.");
            return false;
        }    
        return true;
    }

    public boolean isValidTransfer(String source, String destination, String amount) {
        if (checkBalance(source) - GAS_FEE < Integer.parseInt(amount)) {

            return false;
        }
        return true;
    }

    public void createAccount(String strPublicKey) {
        if(!isLeaderInit){
            initializeLeader();
        }
        PublicKey publicKey = getPublicKey(strPublicKey);
        accounts.put(publicKey, INITIAL_BALANCE);
        payToProducer(GAS_FEE);

    }

    public void transfer(String source, String destination, String amount) {
        updateBalance(source, -Integer.parseInt(amount)  - GAS_FEE);
        updateBalance(destination, Integer.parseInt(amount));
        
        payToProducer(GAS_FEE);
    }

    public Integer checkBalance(String strPublicKey) {
        int balance = -1;

        PublicKey publicKey = getPublicKey(strPublicKey);

        balance = accounts.get(publicKey);
       
        return balance;
    }

    public void updateBalance(String strPublicKey, int amount) {
        
        PublicKey publicKey = getPublicKey(strPublicKey);

        int old = accounts.get(publicKey);
        int updated = old + amount;
        accounts.replace(publicKey, updated);
    }

    public void payToProducer(int amount){
        int old = accounts.get(getLeaderKey());
        int updated = old + amount;
        accounts.replace(getLeaderKey(), updated);
    }

    public PublicKey getPublicKey(String strPublicKey) {
        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get("src/AccountKeys/pubKeyClient" + strPublicKey));
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            return publicKey;
        } catch (Exception e) {
            System.err.println("Error accessing account's public key");
        }
        return null;
    }
    
    public PublicKey getLeaderKey() {
        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get("src/ServerKeys/pubKeyServer1"));
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            return publicKey;
        } catch (Exception e) {
            System.err.println("Error accessing leader's public key");
        }
        return null;
    }

    public Hashtable<PublicKey, Integer> getAccounts() {
        return accounts;
    }

    public void initializeLeader(){
        accounts.put(getLeaderKey(), INITIAL_BALANCE);
        isLeaderInit = true;
    }

}
