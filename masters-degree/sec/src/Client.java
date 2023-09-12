package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    // Communicates through 
    private PerfectLink channel;

    // Stores all available servers
    private ArrayList<HostInfo> servers = new ArrayList<>();

    // Client info
    private HostInfo clientInfo;

    private int commandCounter = 0;

    private ArrayList<Integer> blockIds = new ArrayList<>();
    private ArrayList<Message> blockConfirms = new ArrayList<>();
 
    public Client(int id, InetAddress address, int port) {
        this.channel = new PerfectLink(address, port);
        this.clientInfo = new HostInfo(id, address, port);

        if (!findServers())
            System.exit(-1);

        start();
    }

    // F + 1
    private ArrayList<Message> waitForQuorum() {
        int counter = 0;
        int size = servers.size();

        while (true) {
            Message message = channel.receive();

            int srcId = message.getRequest().getSource().getId();

            if (!blockIds.contains(srcId)) {
                blockIds.add(srcId);
                blockConfirms.add(message);
                counter ++;
            }
            if(counter > size - size/3 + 1){
                blockIds.clear();
                return blockConfirms;
            } 



        }
    }

    // F + 1
    private Message waitForReply(Request req) {
        int counter = servers.size();

        ArrayList<Message> replies = new ArrayList<>();

        while (true) {
            Message message = channel.receive();

            System.out.println(message.getRequest().getTimestamp() + " vs " + req.getTimestamp());

            if (message.getRequest().getTimestamp() == req.getTimestamp()) {
                replies.add(message);
                counter--;
            }

            if (counter == 1)
                break;
        }
        return replies.get(0);
    }

    // Read config file and store members info
    private boolean findServers() {
        try {
            Scanner scanner = new Scanner(new File("../config.txt"));

            while (scanner.hasNextLine()) 
                servers.add(new HostInfo(scanner.nextLine()));
            
            scanner.close();

            return !servers.isEmpty();
        } catch (FileNotFoundException e) {
            System.err.println("Config file not found!");
            
        } catch (Exception e) {
            System.err.println("Error while reading the config file!");
        }
        return false;
    }

    private Message send(String[] command, boolean singleReply) {
        RequestCommand request = new RequestCommand(clientInfo, null, "CLIENT", command, System.currentTimeMillis());

        servers.forEach(server -> {
            request.setDestination(server);
            byte[] signatureBytes = generateSignature(request);
            
            // Send message
            channel.send(request, signatureBytes);
        });
        if(!singleReply){
            // Wait for reply
            return waitForReply(request);
        }
        while(true){
            Message message = channel.receive();
            if (message.getRequest().getTimestamp() == request.getTimestamp()) {
                return message;
            }
        }
    }

    private Message weakSend(String[] command, int replica){
        long timeStamp = System.currentTimeMillis();
        RequestCommand request = new RequestCommand(clientInfo, null, "CLIENT", command, timeStamp);
        System.out.println(servers.get(replica - 1));
        request.setDestination(servers.get(replica - 1));
        byte[] signatureBytes = generateSignature(request);
        channel.send(request, signatureBytes);
        
        while(true){
            Message message = channel.receive();
            System.out.println(message.getRequest().getTimestamp() + " vs " + timeStamp);
            if (message.getRequest().getTimestamp() == timeStamp) {
                return message;
            }
        }
    }

    public void start() {
        String[] options = {"Availabe options:", "1) Create Account", "2) Transfer", "3) Check Balance", "0) Exit"};

        System.out.println("Welcome Client!\n");

        Scanner scanner = new Scanner(System.in);

        boolean exit = false;

        while (true) {

            for (String option : options)
                System.out.println(option);
            
            System.out.print("\nSelect an option: ");

            String option = scanner.nextLine();

            // Command to be sent to server
            String[] command;
        
            switch (option) {
                case "1": {
                    System.out.print("Enter a name for your account:");
                    String accountName = scanner.nextLine();

                    if (generateAccountKeys(accountName)) {
                        System.out.println("Account keys generated for this account!\n");

                        command = new String[]{"CREATE", clientInfo.getId() + "_" + accountName};

                        // Wait for reply
                        Message reply = send(command, true);
                        commandCounter ++;
                        checkCommandCounter();

                        System.out.println("Server replied with: " + reply.getRequest());
                    }
                    else
                        System.err.println("Error while generating account keys!");
                    break;
                }
                    
                case "2": {
                    System.out.println("Enter the name of the account you want to transfer from:>>>");
                    String fromAccountName = scanner.nextLine();

                    System.out.println("Enter the name of the acoount you want to transfer to:>>>");
                    String toAccountName = scanner.nextLine();

                    System.out.println("Enter the amount you want to transfer: ");
                    String amount = scanner.nextLine();

                    command = new String[]{"TRANSFER", clientInfo.getId() + "_" + fromAccountName, clientInfo.getId() + "_" + toAccountName, amount};

                    // Wait for reply
                    Message reply = send(command, true);
                    commandCounter ++;
                    checkCommandCounter();

                    System.out.println("Server replied with: " + reply.getRequest());

                    break;
                }
                    
                case "3": {
                    System.out.print("Enter the name of your account: ");
                    String accountName = scanner.nextLine();

                    System.out.print("Which read do you want to use? (1) Strongly consistent, (2) Weakly consistent: ");
                    String readMode = scanner.nextLine();

                    command = new String[]{"CHECK_BALANCE", clientInfo.getId() + "_" + accountName, readMode};
                    Message reply = null;
                    // Wait for reply
                    if (readMode.equals("1")) {
                        reply = send(command, false);
                    } else if(readMode.equals("2")) {
                        System.out.print("Which replica do you want to read? : [1 - " + servers.size() + "]");
                        String replica = scanner.nextLine();
                        reply = weakSend(command, Integer.parseInt(replica));
                    }
                        

                    System.out.println("Server replied with: " + reply.getRequest());

                    Request req = reply.getRequest();

                   
                    if (req instanceof RequestMessage)
                        System.out.println( ((RequestMessage) req).getValue() );

                    /*
                    else if (req instanceof RequestSnapshot) { 
                        System.out.println("REQUESTSNAPSHOT");
                        RequestSnapshot reqSnap = (RequestSnapshot) req;

                        boolean isValid = true;
                        System.out.println("SIZE: " + reqSnap.getSnapshot().getSignatures().size());
                        for (int i = 0; i < reqSnap.getSnapshot().getSignatures().size(); i++) {
                            int id = reqSnap.getSnapshot().getSignatureOwner().get(i);
                            byte[] signatureFromOwner = reqSnap.getSnapshot().getSignatures().get(i);
                            if (!verifySignature(reqSnap.getSnapshot(), id, signatureFromOwner)){
                                isValid = false;
                            }
                        }
                        if (isValid) {
                            try {
                                byte[] publicKeyBytes = Files.readAllBytes(Paths.get("src/AccountKeys/pubKeyClient" + clientInfo.getId() + "_" + accountName));
                                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

                                int balance = reqSnap.getSnapshot().getAccountBalance(publicKey);
                                System.out.println("Balance: " + balance);
                            } catch (Exception e) {
                                System.err.println("Error");
                            }
                        } 
                    }
                    */
                    
                    break;
                }
                    
                case "0": {
                    System.out.println("Bye!");
                    exit = true;
                    break;
                }
                    
                default: {
                    System.out.println("Invalid option!");
                    break;
                }
            }

            if (exit)
                break;

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();

            // Clear terminal
            System.out.print("\033[H\033[2J");  
        }

        scanner.close();
    }

    // Generate keys for the account
    private boolean generateAccountKeys(String accountID) {
        String id = clientInfo.getId() + "_" + accountID;
        try {
            RSAKeyGenerator keyGen = new RSAKeyGenerator("w", "src/AccountKeys/privKeyClient" + id, "src/AccountKeys/pubKeyClient" + id);
            keyGen.run();
        } catch (Exception e) {
            System.err.println("Error while generating the keys!");
            return false;
        }
        return true;
    }

    // Client only send Requests with commands
    private byte[] generateSignature(RequestCommand req){
        String id = req.getCommand()[1];
        try {
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get("src/AccountKeys/privKeyClient" + id));
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(req.getBytes());

            return signature.sign();

        } catch (Exception e){
            System.out.println(req);
            System.err.println("Deu cocó!");
        }
        return null;
    }

    private boolean verifySignature(Snapshot snapshot, int id, byte[] signatureFromOwner) {
        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get("src/ServerKeys/privKeyServer" + id));
            System.out.println("\n After getting publicKeyBytes from: " + id + "\n");

            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            System.out.println("\n After getting publicKey \n");

            Signature signature = Signature.getInstance("SHA256withRSA");
            System.out.println("\n After getting signature \n");
            signature.initVerify(publicKey);
            System.out.println("\n After initverify with publicKey \n");
            signature.update(snapshot.getAccounts().getBytes());
            System.out.println("\n After update \n");

            return signature.verify(signatureFromOwner);

        } catch (IOException | NoSuchAlgorithmException  | SignatureException | InvalidKeySpecException | InvalidKeyException e) {
            System.err.println(this + "Error while verifying the signature for request: ");
        }
        return false;
    }

    private void checkCommandCounter(){
        ArrayList<Message> responses = null;
        if(commandCounter >= 10){
            responses = waitForQuorum();
            blockConfirms.clear();
            commandCounter = 0;
            for (Message message : responses) {
                RequestMessage req = (RequestMessage) message.getRequest();
                System.out.println(req.getValue());
            }
        }
        
    }

    public static void main(String args[]) {  
        InetAddress address;
        int port;
        int id;

        try {
            id = Integer.parseInt(args[0]);
            port = Integer.parseInt(args[2]);
            address = InetAddress.getByName(args[1]);

        } catch (NumberFormatException | UnknownHostException e) {
            System.err.println("Proper usage is: java Server <int: id> <string: address> <int: port>");
            return;
        }
        new Client(id, address, port);
    }
}
