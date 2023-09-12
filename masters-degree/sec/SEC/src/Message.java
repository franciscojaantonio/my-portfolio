package src;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {
    // About the message
    private String id;

    // Check if the message was already acknowledged
    private boolean isAcknowledged = false;

    // Message request
    private Request request;

    // Message request signature
    private byte[] signatureBytes;

    // For signed messages
    public Message(String id, Request request, byte[] signatureBytes) {
        this.id = id;
        this.request = request;
        this.signatureBytes = signatureBytes;
    }

    // For general messages
    public Message(String id, Request request) {
        this.id = id;
        this.request = request;
        this.signatureBytes = null;
    }

    public String getId() {
        return id;
    }

    public boolean isAcknowledged() {
        return isAcknowledged;
    }

    public void acknowledge() {
        isAcknowledged = true;
    }

    public Request getRequest() {
        return request;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    public InetAddress getSrcAddress() {
        return request.getSource().getAddress();
    }

    public int getSrcPort() {
        return request.getSource().getPort();
    }

    public int getSrcId() {
        return request.getSource().getId();
    }

    @Override
    public String toString() {
        return "Message{ID: " + id + ", REQUEST: " + request + ", ACKNOWLEDGED: '" + isAcknowledged + "'}";
    }
    
}
