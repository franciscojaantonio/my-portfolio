import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class DataClient extends JFrame {
    // Constantes
    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 300;
    // Variáveis da GUI
    private JTextField position_textField, length_textField;
    private JTextArea results_textArea;
    // Conexão e comunicação
    private final InetAddress addressToConnect;
    private final int portToConnect;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public DataClient(InetAddress addressToConnect, int portToConnect) {
        setTitle("Client");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addressToConnect = addressToConnect;
        this.portToConnect = portToConnect;
        addComponents();
        new DealWithByteBlockRequest().start();
    }

    public void open() {
        setVisible(true);
    }

    // Verificar se está contido entre 0 e maxLength
    private boolean isByteBlockInBounds(int index, int length, int maxLength) {
        return (index >= 0 && (index + length - 1) < maxLength);
    }

    public class DealWithByteBlockRequest extends Thread {
        @Override
        public void run() {
            runDataClient();
        }
    }

    public void runDataClient() {
        try {
            connectToStorageNode();
            receiveInfo();
        } catch (IOException e) {
            System.err.println("Erro ao conectar/comunicar com o StorageNode");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erro ao tentar fechar a socket");
            }
        }
    }

    public void connectToStorageNode() throws IOException {
        socket = new Socket(addressToConnect.getHostAddress(), portToConnect);
        //System.out.println("Socket: " + socket);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void receiveInfo() throws IOException {
        while (true) {
            try {
                CloudByte [] result = (CloudByte[]) in.readObject();
                System.out.println("Resultados obtidos com sucesso\n");
                results_textArea.setText("Resultado:\n" + Arrays.toString(result));
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Erro ao tentar ler resposta do StorageNode = { " + addressToConnect.getHostAddress() + " | " + portToConnect + " }");
                break;
            }
        }
    }

    public void search() {
        int position, length;
        try {
            position = Integer.parseInt(position_textField.getText());
            length = Integer.parseInt(length_textField.getText());
        } catch (NumberFormatException e) {
            results_textArea.setText("Os argumentos introduzidos são inválidos!");
            return;
        }
        if(!isByteBlockInBounds(position, length, StorageNode.STORED_DATA_LENGTH)) {
            results_textArea.setText("Os argumentos introduzidos são inválidos!");
            return;
        }
        try {
            out.writeObject(new ByteBlockRequest(position, length));
            System.out.println("Foi feita uma consulta na posição " + position + " com comprimento " + length);
        } catch (IOException e) {
            System.err.println("Erro ao efetuar o pedido ao StorageNode = { " + addressToConnect.getHostAddress() + " | " + portToConnect + " }");
        }
    }

    public void addComponents() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        position_textField = new JTextField("1000", 20);
        length_textField = new JTextField("10", 20);
        results_textArea = new JTextArea("Respostas aparecerão aqui...");
        results_textArea.setEditable(false);
        results_textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JButton search_button = new JButton("Consultar");
        search_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        panel.add(new JLabel("Posição a consultar: "));
        panel.add(position_textField);
        panel.add(Box.createHorizontalStrut(25));
        panel.add(new JLabel("Comprimento: "));
        panel.add(length_textField);
        panel.add(Box.createHorizontalStrut(25));
        panel.add(search_button);
        add(panel, BorderLayout.NORTH);
        add(results_textArea);
    }

    public static void main(String[] args) {
        // Fazer verificações
        InetAddress address = null;
        int port = -1;
        try {
            address = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        } catch (UnknownHostException e) {
            System.out.println("Endereço inválido");
        } catch (NumberFormatException e) {
            System.out.println("Porto inválido");
        }
        DataClient dataClient = new DataClient(address, port);
        dataClient.open();
    }
}

