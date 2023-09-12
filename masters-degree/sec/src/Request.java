package src;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public abstract class Request implements Serializable {
    private long timestamp;

    // Request information
    private String type;

    // Source
    private HostInfo source;

    // Destination
    private HostInfo destination;

    public Request(HostInfo source, HostInfo destination, String type, long timestamp) {
        this.type = type;
        this.source = source;
        this.timestamp = timestamp;
        this.destination = destination;
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            System.err.println("Could not serialize the object!");
        }
        return null;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public HostInfo getSource() {
        return source;
    }

    public HostInfo getDestination() {
        return destination;
    }

    public void setDestination(HostInfo destination) {
        this.destination = destination;

    }

}
