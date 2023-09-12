package src;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostInfo implements Serializable {
    // Host information
    private InetAddress address;
    private int port;
    private int id;

    public HostInfo(int id, InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.id = id;
    }

    public HostInfo(String HostInfo) {
        String[] info = HostInfo.split("[ :]");

        try {
            address = InetAddress.getByName(info[1]);
            port = Integer.parseInt(info[2]);
            id = Integer.parseInt(info[0]);

        } catch (UnknownHostException | NumberFormatException e) {
            System.err.println("Error parsing host information");
            return;
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return address + ":" + port;
    }
    
}
