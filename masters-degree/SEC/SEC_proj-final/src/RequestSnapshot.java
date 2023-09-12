package src;

public class RequestSnapshot extends Request {

    // Snapshot
    private Snapshot snapshot;

    public RequestSnapshot(HostInfo source, HostInfo destination, String type, Snapshot snapshot, long timestamp) {
        super(source, destination, type, timestamp);
        this.snapshot = snapshot;    
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public String toString() {
        return "REQUEST [FROM = " + this.getSource() + ", TO = " + this.getDestination() + ", TYPE = " + this.getType() + ", SNAPSHOT = " + snapshot + "]";
    }

}
