import java.net.InetAddress;

public class StorageNodeInfo {
    private final InetAddress address;
    private final int port;
    private boolean validForConnection;

    public StorageNodeInfo(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.validForConnection = true;
    }

    public boolean isValidForConnection() {
        return validForConnection;
    }

    public void setValidForConnection(boolean validForConnection) {
        this.validForConnection = validForConnection;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "StorageNode { " + address.getHostAddress() + " , " + port + " }";
    }
}
