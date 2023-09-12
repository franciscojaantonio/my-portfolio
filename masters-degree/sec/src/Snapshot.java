package src;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class Snapshot implements Serializable {
    // TES State
    private SnapshotContent content;
    private int accountsHash;

    // Signatures
    private ArrayList<byte[]> signatures = new ArrayList<>();
    private ArrayList<Integer> signatureOwner = new ArrayList<>();

    public Snapshot(Hashtable<PublicKey, Integer> accounts) {
        this.content = new SnapshotContent(accounts);
        this.accountsHash = Objects.hash(content.accountsKeys, content.accountsValues);
    }

    public Snapshot(SnapshotContent accounts) {
        this.content = accounts;
        this.accountsHash = Objects.hash(content.accountsKeys, content.accountsValues);
    }

    public void addSignature(byte[] signature, int signOwner) {
        signatures.add(signature);
        signatureOwner.add(signOwner);
    }

    public SnapshotContent getAccounts() {
        return content;
    }

    public int getAccountsHash() {
        return accountsHash;
    }

    public byte[] getSignature() {
        return signatures.get(0);
    }

    public ArrayList<byte[]> getSignatures() {
        return signatures;
    }

    public ArrayList<Integer> getSignatureOwner() {
        return signatureOwner;
    }

    public int getAccountBalance(PublicKey key) {
        int index = content.accountsKeys.indexOf(key);
        System.out.println("Index = " + index);
        return content.accountsValues.get(index);
    }

    @Override
    public String toString() {
        return "Snapshot [Accounts: " + content.accountsKeys.size() + ", Signatures: " + signatures.size() + "]";
    }

    public class SnapshotContent implements Serializable {
        // TES State
        private ArrayList<PublicKey> accountsKeys = new ArrayList<>();
        private ArrayList<Integer> accountsValues = new ArrayList<>();

        public SnapshotContent(Map<PublicKey, Integer> accounts) {
            for (Map.Entry<PublicKey, Integer> entry : accounts.entrySet()) {
                accountsKeys.add(entry.getKey());
                accountsValues.add(entry.getValue());
            }
        }

        public byte[] getBytes() {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
    
                oos.writeObject(this);
                return bos.toByteArray();
            } catch (IOException e) {
                System.err.println("Could not serialize the Snapshot Content!");
            }
            return null;
        }
    }
    
}
