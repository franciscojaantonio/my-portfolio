package src;

public class RequestCommand extends Request {

    // Instruction [0 -> Command, 1...N -> Arguments]
    private String[] command;

    public RequestCommand(HostInfo source, HostInfo destination, String type, String[] command, long timestamp) {
        super(source, destination, type, timestamp);
        this.command = command;
    }

    public String[] getCommand() {
        return command;
    }

    public void setCommandAt(int index, String value) {
        this.command[index] = value;
    }

    @Override
    public String toString() {
        return "REQUEST [FROM = " + this.getSource() + ", TO = " + this.getDestination() + ", TYPE = " + this.getType() + ", COMMAND = " + command + ", TIME = "+ this.getTimestamp() +"]";
    }
    
}
