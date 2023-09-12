import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StorageNode {
    // Constantes
    public static final int STORED_DATA_LENGTH = 1000000;
    public static final int DOWNLOAD_BLOCK_LENGTH = 100;
    public static final int MIN_NUMBER_ANSWER_TO_CORRECT = 2;
    public static final int NUMBER_OF_DATA_MAINTENANCE_THREADS = 2;
    // Dados Armazenados
    private boolean isDataStored = false;
    private boolean isReady = false;
    private CloudByte[] storedData;
    // Conexão e comunicação
    private final InetAddress dirAddress;
    private final int dirPort, ownPort;
    private BufferedReader dir_in;
    private PrintWriter dir_out;
    private Socket dir_socket;
    // Informação sobre outros StorageNodes inscritos
    private LinkedList<StorageNodeInfo> storageNodes;
    // Lista com a posição dos Bytes a ser corrigidos
    private SynchronizedList<Integer> errorList;

    // Para obter dados vias outros nodes
    public StorageNode(InetAddress dirAddress, int dirPort, int ownPort) {
        storedData = new CloudByte[STORED_DATA_LENGTH];
        errorList = new SynchronizedList<>();
        storageNodes = new LinkedList<>();
        this.dirAddress = dirAddress;
        this.dirPort = dirPort;
        this.ownPort = ownPort;
    }

    // Para obter dados via ficheiro
    public StorageNode(InetAddress dirAddress, int dirPort, int ownPort, String dataFileName) {
        storageNodes = new LinkedList<>();
        errorList = new SynchronizedList<>();
        storedData = new CloudByte[STORED_DATA_LENGTH];
        if (new File(dataFileName).exists()) {
            try {
                byte[] fileContent = Files.readAllBytes(new File(dataFileName).toPath());
                for (int i = 0; i < STORED_DATA_LENGTH; i++)
                    storedData[i] = new CloudByte(fileContent[i]);
                System.out.println("[Inicialização] Todos os dados foram lidos do ficheiro: '" + dataFileName + "'");
                this.isDataStored = true;
            } catch (IOException e) {
                System.err.println("[Inicialização] Erro ao tentar ler o ficheiro: '" + dataFileName + "'");
                storedData = new CloudByte[STORED_DATA_LENGTH];
            }
        } else
            System.err.println("[Inicialização] Não foi encontrado o ficheiro especificado");
        this.dirAddress = dirAddress;
        this.dirPort = dirPort;
        this.ownPort = ownPort;
    }

    private void startSupportThreads() {
        if(!isDataStored)
            return;
        new CorruptDataThread().start();
        new DealWithDataClientThread().start();
        int aux = STORED_DATA_LENGTH / NUMBER_OF_DATA_MAINTENANCE_THREADS;
        for (int i = 0; i < NUMBER_OF_DATA_MAINTENANCE_THREADS; i++)
            new DataMaintenanceThread(i * aux).start();
        isReady = true;
        dir_out.println("READY");
    }

    public void runStorageNode() {
        try {
            connectToServer();
            subscribeDirectory();
        } catch (IOException e) {
            System.err.println("[Inicialização] Erro ao conectar/comunicar com o diretório");
        } finally {
            try {
                dir_socket.close();
            } catch (IOException e) {
                System.err.println("[Inicialização] Erro ao tentar fechar a socket");
            }
        }
    }

    private void connectToServer() throws IOException {
        dir_socket = new Socket(dirAddress.getHostAddress(), dirPort);
        //System.out.println("Socket: " + dir_socket);
        dir_in = new BufferedReader(new InputStreamReader(dir_socket.getInputStream()));
        dir_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(dir_socket.getOutputStream())), true);
    }

    private void subscribeDirectory() throws IOException {
        String subscribe = "INSC " + InetAddress.getLocalHost().getHostAddress() + " " + ownPort;
        dir_out.println(subscribe);
        try {
            dir_out.println("nodes");
            receiveDirInfo();
        } catch (NullPointerException e) {
            System.err.println("[Inicialização] Este StorageNode já existe no diretório");
        }
    }

    private void receiveDirInfo() throws IOException, NullPointerException {
        String readFromServer;
        LinkedList<StorageNodeInfo> temp = new LinkedList<>();
        while (true) {
            readFromServer = dir_in.readLine();
            if (!Objects.equals(readFromServer, "end")) {
                try {
                    int port = Integer.parseInt(readFromServer.split(" ")[2]);
                    InetAddress address = InetAddress.getByName(readFromServer.split(" ")[1]);
                    StorageNodeInfo aux = new StorageNodeInfo(address, port);
                    // Assinalar que não é válido para ligações por se tratar do próprio StorageNode
                    if (port == ownPort && Objects.equals(InetAddress.getLocalHost().getHostAddress(), address.getHostAddress()))
                        aux.setValidForConnection(false);
                    temp.add(aux);
                } catch (NumberFormatException | UnknownHostException e) {
                    System.err.println("[Comunicação c/ Diretório] Leitura de dados inválida");
                }
            } else if (Objects.equals(readFromServer, "end")) {
                if (temp.isEmpty() && !isDataStored) {
                    System.err.println("[Comunicação c/ Diretório] Não existem StorageNodes registados no diretório\nDessa forma, preciso de ficheiro para armazenar os dados");
                    break;
                }
                updateStoredNodes(temp);
                temp = new LinkedList<>();
                if(!isReady) {
                    if (!isDataStored)
                        new DealWithDataDownload().start();
                    else
                        startSupportThreads();
                }
            }
            //System.out.println(readFromServer);
        }
    }

    // Verificar se está contido entre 0 e maxLength
    private boolean isByteBlockInBounds(int index, int length, int maxLength) {
        return (index >= 0 && (index + length - 1) < maxLength);
    }

    // Pedir novamente o registo dos nodes ao diretório
    private void updateStorageNodes() {
        storageNodes = new LinkedList<>();
        dir_out.println("nodes");
        isStorageNodesUpdated();
    }

    // Colocar a lista atualizada de dados relativamente aos StorageNodes inscritos
    private synchronized void updateStoredNodes(LinkedList<StorageNodeInfo> updatedInfo) {
        storageNodes = updatedInfo;
        notifyAll();
    }

    // Método usado para fazer a thread esperar até a lista estar atualizada
    private synchronized void isStorageNodesUpdated() {
        while (storageNodes.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
    }

    // Thread responsável pela injeção de erros
    public class CorruptDataThread extends Thread {
        @Override
        public void run() {
            System.out.println("[Injeção de erros] Pronto para injetar erros...");
            int byteIndex;
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String injectError = scanner.nextLine();
                String[] injectErrorAsArray = injectError.split(" ");
                if (injectErrorAsArray[0].equals("ERROR") && injectErrorAsArray.length == 2) {
                    try {
                        byteIndex = Integer.parseInt(injectErrorAsArray[1]);
                        if (isByteBlockInBounds(byteIndex, 1, STORED_DATA_LENGTH)) {
                            if (storedData[byteIndex].isParityOk()) {
                                storedData[byteIndex].makeByteCorrupt();
                                System.out.println("[Injeção de erros] Foi injetado um erro no Byte na posição: " + byteIndex + " => isParityOk = " + storedData[byteIndex].isParityOk());
                            } else
                                System.err.println("[Injeção de erros] O Byte na posição " + byteIndex + " já contém erro");
                        } else
                            System.err.println("[Injeção de erros] Posição de Byte inválida");
                    } catch (NumberFormatException e) {
                        System.err.println("[Injeção de erros] Deve ser inserido um valor numérico válido");
                    }
                } else
                    System.err.println("[Injeção de erros] Argumentos inválidos");
            }
        }
    }

    // Thread responsável pela receção de ligações por parte de Clientes e outros StorageNodes
    public class DealWithDataClientThread extends Thread {
        @Override
        public void run() {
            try {
                startServing();
            } catch (IOException e) {
                System.err.println("[Conexão c/ Cliente] Erro ao lançar DealWithDataClientThread");
            }
        }

        public void startServing() throws IOException {
            System.out.println("[Conexão c/ Cliente] Pronto para receber novas conexões...");
            ServerSocket ss = new ServerSocket(ownPort);
            try {
                while (true) {
                    Socket socket = ss.accept();
                    System.out.println("[Conexão c/ Cliente] Nova conexão");
                    new StorageNode.DealWithDataClientThread.DealWithDataClient(socket).start();
                }
            } finally {
                ss.close();
            }
        }

        public class DealWithDataClient extends Thread {
            private ObjectInputStream cli_in;
            private ObjectOutputStream cli_out;
            private Socket cli_socket;

            public DealWithDataClient(Socket cSocket) throws IOException {
                cli_in = new ObjectInputStream(cSocket.getInputStream());
                cli_out = new ObjectOutputStream(cSocket.getOutputStream());
                this.cli_socket = cSocket;
            }

            @Override
            public void run() {
                try {
                    serve();
                } catch (IOException e) {
                    //System.err.println("[Conexão c/ Cliente] Canais de comunicação foram fechados pelo Cliente");
                } finally {
                    try {
                        cli_socket.close();
                        System.err.println("[Conexão c/ Cliente] Ligação Cliente terminada");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void serve() throws IOException {
                while (true) {
                    try {
                        ByteBlockRequest aux = (ByteBlockRequest) cli_in.readObject();
                        //System.out.println("[Conexão c/ Cliente] Um ByteBlockRequest chegou");
                        if (isByteBlockInBounds(aux.getStartIndex(), aux.getLength(), STORED_DATA_LENGTH)) {
                            CloudByte[] result = new CloudByte[aux.getLength()];
                            for (int i = 0; i < aux.getLength(); i++) {
                                if (!storedData[aux.getStartIndex() + i].isParityOk()) {
                                    Thread temp = new DealWithDataCorrection(aux.getStartIndex() + i);
                                    temp.start();
                                    try {
                                        temp.join();
                                    } catch (InterruptedException ignored) {}
                                }
                                result[i] = storedData[aux.getStartIndex() + i];
                            }
                            //System.out.println("\nSerá enviado:\n" + Arrays.toString(result) + "\n");
                            cli_out.writeObject(result);
                            cli_out.reset();
                        } else
                            System.err.println("[Conexão c/ Cliente] Valores de ByteBlockRequest inválidos");
                    } catch (ClassNotFoundException e) {
                        System.err.println("[Conexão c/ Cliente] Leitura inválida de ByteBlockRequest");
                    }
                }
            }
        }
    }

    // Thread responsável pela transferência de dados, caso estes estejam não tenham sido lidos do ficheiro
    public class DealWithDataDownload extends Thread {
        private SynchronizedList<ByteBlockRequest> requests;
        private LinkedList<DealWithDataDownloadThread> dTList;
        private final long initialTime;

        public DealWithDataDownload() {
            dTList = new LinkedList<>();
            requests = new SynchronizedList<>();
            initialTime = System.currentTimeMillis();
        }

        public void sendDataRequests() {
            for (int i = 0; i < STORED_DATA_LENGTH; i += DOWNLOAD_BLOCK_LENGTH)
                requests.add(new ByteBlockRequest(i, DOWNLOAD_BLOCK_LENGTH));
        }

        @Override
        public void run() {
            try {
                sendDataRequests();
                System.out.println("[Download dados] Para transferir o conteúdo serão feitas ligações aos StorageNodes:");
                for (StorageNodeInfo sn : storageNodes) {
                    if (sn.isValidForConnection()) {
                        dTList.add(new DealWithDataDownloadThread(sn));
                        System.out.println(sn);
                    }
                }
                for (DealWithDataDownloadThread d : dTList)
                    d.start();
            } finally {
                for (DealWithDataDownloadThread d : dTList) {
                    try {
                        d.join();
                    } catch (InterruptedException e) {
                        System.err.println("[Download dados] Interrupção durante a transferência");
                    }
                }
                isDataStored = true;
                System.out.println("[Download dados] Todos os dados foram transferidos [" + (System.currentTimeMillis() - initialTime) / 1000 + "s " + "com " + dTList.size() + " threads]");
                startSupportThreads();
            }
        }

        public class DealWithDataDownloadThread extends Thread {
            private final InetAddress storageNodeAddress;
            private final int storageNodePort;
            private ObjectInputStream sto_in;
            private ObjectOutputStream sto_out;
            private Socket sto_socket;
            private int downloadedBlocks;

            public DealWithDataDownloadThread(StorageNodeInfo storageNodeInfo) {
                this.downloadedBlocks = 0;
                this.storageNodeAddress = storageNodeInfo.getAddress();
                this.storageNodePort = storageNodeInfo.getPort();
            }

            @Override
            public void run() {
                runDealWithDataDownload();
            }

            public void runDealWithDataDownload() {
                try {
                    connectToStorageNode();
                    receiveInfo();
                } catch (IOException e) {
                    System.err.println("[Download dados] Erro ao conectar/comunicar com o StorageNode");
                } finally {
                    try {
                        sto_socket.close();
                        //System.out.println("[Download dados] Ligação de download terminada");
                    } catch (IOException e) {
                        System.err.println("[Download dados] Erro ao tentar fechar a socket");
                    }
                }
            }

            public void connectToStorageNode() throws IOException {
                sto_socket = new Socket(storageNodeAddress, storageNodePort);
                //System.out.println("Socket: " + sSocket);
                sto_out = new ObjectOutputStream(sto_socket.getOutputStream());
                sto_in = new ObjectInputStream(sto_socket.getInputStream());
            }

            public void receiveInfo() throws IOException {
                CloudByte[] byteBlock;
                while (!requests.isEmpty()) {
                    ByteBlockRequest temp = requests.removeFirst();
                    if (temp == null)
                        break;
                    try {
                        sto_out.writeObject(temp);
                        byteBlock = (CloudByte[]) sto_in.readObject();
                        for (int i = temp.getStartIndex(); i < temp.getStartIndex() + DOWNLOAD_BLOCK_LENGTH; i++) {
                            storedData[i] = byteBlock[i - temp.getStartIndex()];
                        }
                        downloadedBlocks++;
                        try {
                            sleep(1);
                        } catch (InterruptedException ignored) {
                        }
                        //System.out.println("[Download dados] Recebi parte dos dados! [" + temp.getStartIndex() + "/" + STORED_DATA_LENGTH + "]");
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("[Download dados] Erro ao escrever/ler ByteBlockRequest");
                    }
                }
                System.out.println("[Download dados] Terminei após transferir " + downloadedBlocks + " blocos");
            }
        }
    }

    // Thread responsável por manter os dados corretos
    public class DataMaintenanceThread extends Thread {
        private int byteIndex;

        public DataMaintenanceThread(int i) {
            this.byteIndex = i;
            System.out.println("[Manutenção de dados] Pronto para detetar erros (posição inicial " + byteIndex + ")");
        }

        @Override
        public void run() {
            while (true) {
                if (storedData[byteIndex] == null) {
                    System.err.println("[Manutenção de dados] Detetado valor nulo na posição: " + byteIndex);
                }
                if (!storedData[byteIndex].isParityOk())
                    try {
                        System.err.println("[Manutenção de dados] Foi detetado um erro na posição " + byteIndex + ": " + storedData[byteIndex]);
                        new DealWithDataCorrection(byteIndex).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                this.byteIndex++;
                if (byteIndex == STORED_DATA_LENGTH) {
                    this.byteIndex = 0;
                    System.err.println("[Manutenção de dados] A deteção de erros voltou à posição 0...");
                }
                try {
                    sleep(1);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    // Thread responsável pela correção de Bytes
    public class DealWithDataCorrection extends Thread {
        CountDownLatch ctl = new CountDownLatch(MIN_NUMBER_ANSWER_TO_CORRECT);
        SynchronizedList<CloudByte> results;
        Lock lock = new ReentrantLock();
        private int byteIndex;

        public DealWithDataCorrection(int index) {
            this.byteIndex = index;
            this.results = new SynchronizedList<>();
        }

        // Poderia ser synchronized
        public void addByteForCorrection(CloudByte[] byteBlock) {
            lock.lock();
            try {
                if (results.containsOrEmpty(byteBlock[0]) && ctl.getCount() > 0 && byteBlock[0].isParityOk()) {
                    System.out.println("[Correção Dados] Byte de correção: " + byteBlock[0] + " => isParityOk = " + byteBlock[0].isParityOk());
                    results.add(byteBlock[0]);
                    ctl.countDown();
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            if (!errorList.addIfEmptyOrNotContains(byteIndex)) {
                System.err.println("[Correção Dados] O Byte " + byteIndex + " já está a ser corrigido");
                return;
            }
            updateStorageNodes();
            if ( (storageNodes.size() - 1) <= 1) {
                System.err.println("[Correção Dados] StorageNodes insuficientes para correção");
                errorList.removeObject(byteIndex);
                return;
            }
            System.out.println("[Correção Dados] Para corrigir o Byte na posição " + byteIndex + ", serão feitas ligações aos StorageNodes:");
            for (StorageNodeInfo sn : storageNodes) {
                if (sn.isValidForConnection()) {
                    System.out.println(sn);
                    new DealWithDataCorrectionThread(sn, byteIndex).start();
                }
            }
            try {
                //ctl.await();
                /* Utilizámos esta variante do ctl.await() para os casos em que existem StorageNodes
                   suficientes para a correção, mas não foram cumpridos os critérios de correção (x respostas certas).
                   Assim, existe um timeout de 4s que, se chegar ao fim, indica a impossibilidade de proceder à correção do Byte */
                if (ctl.await(4, TimeUnit.SECONDS)) {
                    storedData[byteIndex] = results.getFirst();
                    System.out.println("[Correção Dados] Byte na posição " + byteIndex + " corrigido para: " + storedData[byteIndex] + " => isParityOk = " + storedData[byteIndex].isParityOk());
                } else
                    System.err.println("[Correção Dados] Respostas insuficiente para a correção do Byte na posição " + byteIndex);
                errorList.removeObject(byteIndex);
            } catch (InterruptedException e) {
                System.err.println("[Correção Dados] Interrupção");
            }
        }

        public class DealWithDataCorrectionThread extends Thread {
            private ObjectInputStream err_in;
            private ObjectOutputStream err_out;
            private Socket err_socket;
            private final InetAddress storageNodeAddress;
            private final int storageNodePort;

            public DealWithDataCorrectionThread(StorageNodeInfo storageNodeInfo, int index) {
                byteIndex = index;
                this.storageNodeAddress = storageNodeInfo.getAddress();
                this.storageNodePort = storageNodeInfo.getPort();
            }

            @Override
            public void run() {
                runDealWithDataCorrection();
            }

            public void runDealWithDataCorrection() {
                try {
                    connectToStorageNode();
                    receiveInfo();
                } catch (IOException e) {
                    System.err.println("[Correção Dados] Erro ao conectar/comunicar com o StorageNode");
                } finally {
                    try {
                        err_socket.close();
                        System.out.println("[Correção Dados] Ligação de download terminada");
                    } catch (IOException e) {
                        System.err.println("[Correção Dados] Erro ao tentar fechar a socket");
                    }
                }
            }

            public void connectToStorageNode() throws IOException {
                err_socket = new Socket(storageNodeAddress, storageNodePort);
                //System.out.println("Socket: " + err_socket);
                err_out = new ObjectOutputStream(err_socket.getOutputStream());
                err_in = new ObjectInputStream(err_socket.getInputStream());
            }

            public void receiveInfo() throws IOException {
                CloudByte[] byteBlock;
                try {
                    err_out.writeObject(new ByteBlockRequest(byteIndex, 1));
                    byteBlock = (CloudByte[]) err_in.readObject();
                    //System.out.println("[Correção Dados] Recebi como Byte de correção: " + byteBlock[0] + " => isParityOk = " + byteBlock[0].isParityOk());
                    addByteForCorrection(byteBlock);
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("[Correção Dados] Erro ao escrever/ler ByteBlockRequest");
                }
            }
        }
    }

    public static void main(String[] args) {
        // Fazer verificações
        if (args.length < 3 || args.length > 4) {
            System.err.println("Argumentos inválidos na criação do StorageNode");
            return;
        }
        InetAddress dirAddress = null;
        int dirPort = -1;
        int ownPort = -1;
        try {
            dirAddress = InetAddress.getByName(args[0]);
            dirPort = Integer.parseInt(args[1]);
            ownPort = Integer.parseInt(args[2]);
        } catch (UnknownHostException e) {
            System.err.println("Endereço inválido na criação do StorageNode");
        } catch (NumberFormatException e) {
            System.err.println("Porto inválido na criação do StorageNode");
        }
        StorageNode storageNode;
        if (args.length == 4) {
            storageNode = new StorageNode(dirAddress, dirPort, ownPort, args[3]);
        } else {
            storageNode = new StorageNode(dirAddress, dirPort, ownPort);
        }
        storageNode.runStorageNode();
    }
}
