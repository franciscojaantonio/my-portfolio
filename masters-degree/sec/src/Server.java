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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

public class Server {
    // TES
    private TES tes;

    // BlockChain
    private LinkedList<Block> blockChain = new LinkedList<>();
    private Block currentBlock = new Block(0);
    //private int blockCounter = 0;

    // Communicates through 
    private PerfectLink channel;

    // About the server
    private HostInfo serverInfo;

    private boolean isLeader = false;

    private int behavior;

    // About all the members
    private int NUM_MEMBERS;
    
    private ArrayList<HostInfo> members = new ArrayList<>();

    // Holds the client requests that are waiting to be processed
    //private ArrayList<Message> clientRequests = new ArrayList<>();

    // Algorithm
    private ArrayList<Integer> prepareIds = new ArrayList<>();
    private ArrayList<Integer> prepareValues = new ArrayList<>();
    private ArrayList<RequestBlock> prepareRequests = new ArrayList<>();
     
    private ArrayList<Integer> commitIds = new ArrayList<>();
    private ArrayList<Integer> commitValues = new ArrayList<>();
    private ArrayList<RequestBlock> commitRequests = new ArrayList<>();

    private ArrayList<RequestSnapshot> prepareSnapshotRequests = new ArrayList<>();

    // Signatures mechanism
    private PrivateKey privateKey;
    private Signature signature;


    // Last snapshot
    //private Snapshot currentSnapshot;

    // Client request being processed
    //private Message currentRequest = null;
    
    public Server(int id, InetAddress address, int port, int behavior) {

        this.serverInfo = new HostInfo(id, address, port);
        this.channel = new PerfectLink(address, port);
        this.behavior = behavior;

        if (id == 1){
            isLeader = true;
        }

        // Check if keys were generated and members were found
        if (!generateKeys() || !findMembers())
            System.exit(-1);

        // Print status
        System.out.println("[Server # " + id + "] Keys generated and members found!\n");

        tes = new TES();

        // Thread to keep the server listening
        new Listener().start();
    }

    // Generate keys for the server & get instance of signature
    private boolean generateKeys() {
        int id = serverInfo.getId();
        try {
            RSAKeyGenerator keyGen = new RSAKeyGenerator("w", "src/ServerKeys/privKeyServer" + id, "src/ServerKeys/pubKeyServer" + id);
            keyGen.run();

            byte[] privateKeyBytes = Files.readAllBytes(Paths.get("src/ServerKeys/privKeyServer" + id));
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            signature = Signature.getInstance("SHA256withRSA");

        } catch (Exception e) {
            System.err.println("Error while generating the keys!");
            return false;
        }
        return true;
    }

    // Read config file and store members info
    private boolean findMembers() {
        try {
            Scanner scanner = new Scanner(new File("../config.txt"));

            while (scanner.hasNextLine()) 
                members.add(new HostInfo(scanner.nextLine()));
            
            scanner.close();

            NUM_MEMBERS = members.size();

            return !members.isEmpty();
        } catch (FileNotFoundException e) {
            System.err.println("Config file not found!");
            
        } catch (Exception e) {
            System.err.println("Error while reading the config file!");
        }
        return false;
    }

    // Generate signature for a request
    private byte[] generateSignature(Request req) {
        try {
            signature.initSign(privateKey);
            signature.update(req.getBytes());
            return signature.sign();

        } catch (InvalidKeyException | SignatureException e) {
            System.err.println("Error while generating the signature for request: " + req);
        }
        return null;
    }

    // Generate signature for a request
    /*
    private byte[] generateSnapshotMapSignature(Snapshot snapshot) {
        try {
            signature.initSign(privateKey);
            signature.update(snapshot.getAccounts().getBytes());
            return signature.sign();

        } catch (InvalidKeyException | SignatureException e) {
            System.err.println("Error while generating the signature for snapshot: " + snapshot);
        }
        return null;
    }
     */

    // Verify signature of a request
    private boolean verifySignature(Message msg, String path) {
        try {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(path));
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            signature.initVerify(publicKey);
            signature.update(msg.getRequest().getBytes());

            return signature.verify(msg.getSignatureBytes());

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            System.err.println(this + "Error while verifying the signature for request: " + msg.getRequest());
        }
        return false;
    }

    // Best Effort Broadcast
    public void broadcast(String type, Request req) {
        members.forEach(server -> {
            //Request request = new Request(serverInfo, server, type, req.getTimestamp());
            Request request;

            if (req instanceof RequestBlock)
                request = new RequestBlock(serverInfo, server, type, ((RequestBlock) req).getBlock(), req.getTimestamp());

            else if (req instanceof RequestCommand)
                request = new RequestCommand(serverInfo, server, type, ((RequestCommand) req).getCommand(), req.getTimestamp());

            else
                request = new RequestSnapshot(serverInfo, server, type, ((RequestSnapshot) req).getSnapshot(), req.getTimestamp());
            
            byte[] signatureBytes = generateSignature(request);

            if (signatureBytes == null)
                System.err.println("Error while generating the signature for request: " + request);
            else
                channel.send(request, signatureBytes);
        });
    }

    public void handleMessage(Message msg) {
        if(behavior == 4){
            return;
        }
        Request req = msg.getRequest();

        // Set public key path for signature and verify it
        String path;
        
        if (req.getType().equals("CLIENT"))
            path = "src/AccountKeys/pubKeyClient" + ((RequestCommand) req).getCommand()[1];
        else 
            path = "src/ServerKeys/pubKeyServer" + msg.getSrcId();

        boolean isValid = verifySignature(msg, path);

        if (!isValid) {
            System.err.println(this + "Signature not valid for request: " + req);
            return;
        } 
        else {
            System.err.println(this + "Signature valid for request: " + req);
        }   
        
        // Handle request based on type
        switch (req.getType()) {
            case "CLIENT": {
                // Only leader broadcasts pre-prepare
                

                RequestCommand reqCmd = (RequestCommand) req;

                System.out.println(reqCmd.getCommand()[0]);
                if(reqCmd.getCommand()[0].equals("CHECK_BALANCE")) {
                    System.out.println(this + "Received 'CLIENT': " + req);
                    System.out.println(reqCmd.getCommand()[2]);
                    if(reqCmd.getCommand()[2].equals("1")){
                        if (!isLeader)
                            return;
                        System.out.println("CHEGOU AO STRONG");
                        broadcast("PRE-PREPARE", reqCmd);

                    } 
                    
                    else if(reqCmd.getCommand()[2].equals("2")) {
                        System.out.println("Chegou ao else");
                        HostInfo client = new HostInfo("0 127.0.0.1:1230");
                        //RequestSnapshot info = new RequestSnapshot(serverInfo, client, "READ_CONFIRM", currentSnapshot, req.getTimestamp());
                        int balance = tes.checkBalance(reqCmd.getCommand()[1]);
                        RequestMessage reqMessage = new RequestMessage(serverInfo, client, "READ_CONFIRM", Integer.toString(balance), req.getTimestamp());
                        System.out.println("SENDING WEAK READ");
                        channel.send(reqMessage, generateSignature(reqMessage));

                        
                    }
                    break;
                }
                if (!isLeader)
                    break;
                else if(reqCmd.getCommand()[0].equals("CREATE") || reqCmd.getCommand()[0].equals("TRANSFER")){
                    System.out.println(this + "Received 'CLIENT': " + req);
                    addTransaction(reqCmd);
                    HostInfo client = new HostInfo("0 127.0.0.1:1230");
                    RequestMessage reqMessage = new RequestMessage(serverInfo, client, "CONFIRM", "Transactoin added to block", req.getTimestamp());
                    channel.send(reqMessage, generateSignature(reqMessage));
                    break;
                }
                else{
                    sendError(req);
                }
                break;
            }
                
            case "PRE-PREPARE": {
                System.out.println(this + "Received 'PRE-PREPARE': " + req);

                //Strongly consistent read
                if (req instanceof RequestCommand) {
                    RequestCommand reqCmd = (RequestCommand) req;

                    int balance = tes.checkBalance(reqCmd.getCommand()[1]);
                    if(balance != -1){
                        String strBalance = Integer.toString(balance);
                        reqCmd.setCommandAt(1, strBalance);
                        broadcast("PREPARE", reqCmd);
                    }
                    break;
                }
                
                else if (req instanceof RequestBlock) {
                    RequestBlock reqBlock = (RequestBlock) req;

                    currentBlock = reqBlock.getBlock();

                    //RANDOM VALUE BEHAVIOR OR REPLAY ATTACK
                    if(behavior == 2 || behavior == 3){
                        Block corruptedBlock = reqBlock.getBlock();
                        corruptedBlock.addTransaction(corruptedBlock.getTransactions().get(0));
                        RequestBlock corruptedRequest = new RequestBlock(reqBlock.getSource(), reqBlock.getDestination(), reqBlock.getType(), corruptedBlock, NUM_MEMBERS);
                        
                        if(behavior != 3){
                            broadcast("PREPARE", corruptedRequest);
                        }
                        else{
                            int messageNumber = 10;
                            while(messageNumber > 0){
                                broadcast("PREPARE", corruptedRequest);
                                messageNumber --;
                            }
                        }
                    }
                    if(validTransactions()){
                        broadcast("PREPARE", reqBlock);
                    }
                    else {
                        sendError(req);
                        currentBlock = new Block(0);
                    }
                    break;
                }
                else{
                    sendError(req);
                }
                break;
            }
                
            case "PREPARE": {
                //System.out.println(this + "Received 'PREPARE': " + req);

                int srcId = req.getSource().getId();

                if (!prepareIds.contains(srcId)) {
                    prepareIds.add(srcId);

                    if (req instanceof RequestBlock) {
                        RequestBlock reqBlock = (RequestBlock) req;

                        prepareValues.add(reqBlock.getBlockHash());
                        prepareRequests.add(reqBlock);
                    }
                    else if (req instanceof RequestCommand) {
                        RequestCommand reqCmd = (RequestCommand) req;

                        prepareValues.add(Integer.parseInt(reqCmd.getCommand()[1]));
                    }
                    else if (req instanceof RequestSnapshot) {
                        RequestSnapshot reqSnap = (RequestSnapshot) req;
                        prepareValues.add(reqSnap.getSnapshot().getAccountsHash());
                        prepareSnapshotRequests.add(reqSnap);
                    }
                    else{
                        sendError(req);
                    }

                    if (prepareIds.size() >= (NUM_MEMBERS - NUM_MEMBERS/3 + 1)) {
                        System.out.println(this + "Reached quorum on 'PREPARE' with size " + prepareIds.size());
                        // PREPARE Quorum
                        int quorumValue = getQuorumValue(prepareValues);

                        if (req instanceof RequestCommand) {
                            RequestCommand reqCmd = (RequestCommand) req;

                            String returnValue = Integer.toString(quorumValue);
                            HostInfo client = new HostInfo("0 127.0.0.1:1230");
                            RequestMessage confirmation = new RequestMessage(serverInfo, client, "READ_CONFIRM", returnValue, reqCmd.getTimestamp());
                            channel.send(confirmation, null);
                            prepareIds.clear();
                            prepareValues.clear();

                            return;
                        }

                        if (req instanceof RequestBlock) {
                            for (RequestBlock request : prepareRequests) {
                                if(request.getBlockHash() == quorumValue){
                                    broadcast("COMMIT", request);
                                    break;
                                }   
                            }
                            prepareRequests.clear();
                        }
                        /*
                        else {
                            currentSnapshot = null;

                            for (RequestSnapshot request : prepareSnapshotRequests) {

                                if(request.getSnapshot().getAccountsHash() == quorumValue) {
                                    if (currentSnapshot == null) 
                                        currentSnapshot = new Snapshot(request.getSnapshot().getAccounts());

                                    currentSnapshot.addSignature(request.getSnapshot().getSignature(), serverInfo.getId());
                                }
                            }
                            prepareSnapshotRequests.clear();

                            System.out.println("Snapshot: " + currentSnapshot);
                        }
                        */
                        prepareIds.clear();
                        prepareValues.clear();                        
                    }
                }

                break;
            }
                
            case "COMMIT": {
                //System.out.println(this + "Received 'COMMIT': " + req);

                RequestBlock reqBlock = (RequestBlock) req;

                int srcId = reqBlock.getSource().getId();

                if (!commitIds.contains(srcId)) {
                    commitIds.add(srcId);
                    commitValues.add(reqBlock.getBlockHash());

                    if (commitIds.size() >= (NUM_MEMBERS - NUM_MEMBERS/3 + 1)) {
                        System.out.println(this + "Reached quorum on 'COMMIT' with size " + commitIds.size());
                        // Add to chain
                        int quorumBlockHash = getQuorumValue(prepareValues);

                        for (RequestBlock request : commitRequests) {
                            if(request.getBlockHash() == quorumBlockHash){
                                currentBlock = request.getBlock();
                                break;
                            }   
                        }

                        runTransactions();

                        addBlock();
                        /*
                        blockCounter++;

                        if (blockCounter == 3) {
                            Snapshot snapshot = new Snapshot(tes.getAccounts());
                            snapshot.addSignature(generateSnapshotMapSignature(snapshot), serverInfo.getId());
                            RequestSnapshot reqSnapshot = new RequestSnapshot(serverInfo, null, null, snapshot, System.currentTimeMillis());
                            broadcast("PREPARE", reqSnapshot);
                            blockCounter = 0;
                        }
                         */
                        commitIds.clear();
                        commitValues.clear();
                        commitRequests.clear();

                        System.out.println(this + "Chain Updated: " + reqBlock);
                        
                        // To do: Confirm commit
                        RequestMessage confirmation = new RequestMessage(serverInfo, new HostInfo("0 127.0.0.1:1230"), "CONFIRM", "Block added", req.getTimestamp());
                        channel.send(confirmation, null);
                        
                        currentBlock = new Block(0);
                    }
                }
                break;
            }
                
            default: {
                sendError(req);
                break;
            }
        }
    }

    private int getQuorumValue(ArrayList<Integer> list) {
        int max = 0;
        int toCommit = -1;

        for (int value: list) {
            int aux = Collections.frequency(list, value);
            if (aux > max) {
                max = aux;
                toCommit = value;
            }
        }
        return toCommit;
    }
    
    private void addTransaction(RequestCommand transaction){
        currentBlock.addTransaction(transaction);
        if(currentBlock.isReadyToAdd()){
            RequestBlock request = new RequestBlock(serverInfo, null, transaction.getType(), currentBlock, transaction.getTimestamp());
            broadcast("PRE-PREPARE", request);
        }
    }
        
    private void addBlock(){
        int prevHeaderHash;
        try {
            prevHeaderHash = Objects.hashCode(blockChain.getLast().getBlockHeader());
        } catch (NoSuchElementException e) {
            prevHeaderHash = 0;
        } 
        BlockHeader blockHeader = new BlockHeader(0, prevHeaderHash, System.currentTimeMillis());
        Block block = currentBlock;
        block.setBlockHeader(blockHeader);
        blockChain.add(block);
    }

    private void runTransactions(){
        String[] cmd;

        for (RequestCommand transaction : currentBlock.getTransactions()) {
            cmd = transaction.getCommand();

            switch(cmd[0]){
                case "CREATE":
                    tes.createAccount(cmd[1]);
                    break;
                case "TRANSFER":
                    tes.transfer(cmd[1], cmd[2], cmd[3]);
                    break;
                case "CHECK_BALANCE":
                    tes.checkBalance(cmd[1]);
                    break;
                default:
                    break;
            }
        }
    }

    public boolean validTransactions(){
        String[] cmd;

        for (RequestCommand transaction : currentBlock.getTransactions()) {
            cmd = transaction.getCommand();
            
            switch(cmd[0]){
                case "CREATE":
                    if(!tes.isValidCreateAccount(cmd[1])){
                        return false;
                    }
                    break;
                case "TRANSFER":
                    if(!tes.isValidTransfer(cmd[1], cmd[2], cmd[3])){
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private void sendError(Request req){
        HostInfo client = new HostInfo("0 127.0.0.1:1230");
        RequestMessage info = new RequestMessage(serverInfo, client, "INVALID", "Something went wrong!", req.getTimestamp());
        channel.send(info, generateSignature(info));      
    }

    @Override
    public String toString() {
        return "[Server #" + serverInfo.getId() + "] ";
    }

    public class Listener extends Thread {
        @Override
        public void run() {
            System.out.println(Server.this + "Listening on port " + serverInfo.getPort() + "...");
            while(true) {
                
                Message temp = channel.receive();

                handleMessage(temp);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        InetAddress address;
        int id, port, behavior;

        try {
            id = Integer.parseInt(args[0]);
            port = Integer.parseInt(args[2]);
            address = InetAddress.getByName(args[1]);
            behavior = Integer.parseInt(args[3]);

        } catch (NumberFormatException | UnknownHostException e) {
            System.err.println("Proper usage is: java Server <int: id> <string: address> <int: port>");
            return;
        }
        
        new Server(id, address, port, behavior);
    }
}
