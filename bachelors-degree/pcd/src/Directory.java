import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Directory {
    private ServerSocket serverSocket;
    private SynchronizedList<String> storageNodes;

    public Directory(int porto) throws IOException {
        serverSocket = new ServerSocket(porto);
        storageNodes = new SynchronizedList<>();
    }

    public void serve() throws IOException {
        System.out.println("Diretório pronto para receber conexões...");
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                new ReceberCliente(socket).start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao aceitar ligação de cliente no diretório.");
        } finally {
            serverSocket.close();
        }
    }

    public class ReceberCliente extends Thread {
        private Socket clientSocket;
        private String clientAddress;
        private int clientPort;
        private boolean registered;
        private BufferedReader in;
        private PrintWriter out;

        public ReceberCliente(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.registered = false;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                String inscMsg = in.readLine();

                String[] msgArray = inscMsg.split(" ");
                if (msgArray.length >= 2 && msgArray[0].equals("INSC")) {
                    clientAddress = msgArray[1];
                    clientPort = Integer.parseInt(msgArray[2]);
                    System.out.println("O cliente: " + clientAddress + " " + clientPort + ", pretende inscrever-se no diretório");

                    if (!storageNodes.contains(clientAddress + " " + clientPort)) {
                        while (true) {
                            while (true) {
                                String msg = in.readLine();
                                System.out.println("Mensagem recebida: " + msg);
                                if (msg.equals("nodes")) {
                                    nodes(out);
                                    break;
                                }
                            }
                            if (!registered) {
                                if (in.readLine().equals("READY")) {
                                    System.out.println("Cliente inscrito: " + clientAddress + " " + clientPort);
                                    storageNodes.add(clientAddress + " " + clientPort);
                                    registered = true;
                                } else
                                    break;
                            }
                        }
                    }
                    else
                        System.out.println("O cliente: " + clientAddress + " " + clientPort + ", já existe no diretório");
                }
                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException | NullPointerException e) {
                if (registered) {
                    storageNodes.removeObject(clientAddress + " " + clientPort);
                    System.err.println("Ligação com o cliente " + clientSocket.getInetAddress().getHostAddress() + " " + clientPort + " terminada");
                } else
                    System.err.println("Não foi possível registar o cliente " + clientSocket.getInetAddress().getHostAddress() + " " + clientPort);
            }
        }

        private void nodes(PrintWriter out) {
            for (String storageNode : storageNodes.getList())
                out.println("node " + storageNode);
            out.println("end");
        }
    }

    public static void main(String[] args) {
        try {
            new Directory(Integer.parseInt(args[0])).serve();
        } catch (NumberFormatException e) {
            System.err.println("Número de porto inválido");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

