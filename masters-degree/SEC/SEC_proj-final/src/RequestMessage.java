package src;

public class RequestMessage extends Request {

    // Message being sent
    private String value;

    public RequestMessage(HostInfo source, HostInfo destination, String type, String value, long timestamp) {
        super(source, destination, type, timestamp);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "REQUEST [FROM = " + this.getSource() + ", TO = " + this.getDestination() + ", TYPE = " + this.getType() + ", MESSAGE = " + value + "]";
    }
    
}
