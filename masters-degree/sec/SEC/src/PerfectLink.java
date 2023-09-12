package src;

import java.net.InetAddress;
import java.util.HashMap;

public class PerfectLink {
    // Uses
    private StubbornLink stubbornLink;

    // About the server
    private InetAddress address;
    private int port;

    // Message Identifier
    private int messageId = 1;

    // Track delivered messages
    HashMap<String, Message> delivered = new HashMap<>();
    
    public PerfectLink(InetAddress address, int port) {
        this.stubbornLink = new StubbornLink(port);
        this.address = address;
        this.port = port;
    }

    public void send(Request request, byte[] signatureBytes) {
        Message toSend = new Message(getNextId(), request, signatureBytes);

        HostInfo dst = request.getDestination();
        
        stubbornLink.send(dst.getAddress(), dst.getPort(), toSend);
    }

    public Message receive() {
        Message temp;

        while(true) {
            temp = stubbornLink.receive();

            if (temp.isAcknowledged()) {
                stubbornLink.stopSender(temp.getId());
                continue;
            }

            acknowledge(temp);

            if (!delivered.containsKey(temp.getId())) {
                delivered.put(temp.getId(), temp);     
                break;
            }
        }
        return temp;
    }

    public void acknowledge(Message msg) {
        stubbornLink.acknowledge(msg);
    }

    private String getNextId() {
        return address + "_" + port + "_" + messageId++;
    }
}
