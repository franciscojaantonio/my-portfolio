package src;

import java.util.Objects;

public class RequestBlock extends Request {

    // Block
    private Block block = null;
    private int blockHash;

    public RequestBlock(HostInfo source, HostInfo destination, String type, Block block, long timestamp) {
        super(source, destination, type, timestamp);
        this.block = block;
        this.blockHash = Objects.hash(block.getStrTransactions());
    }

    public Block getBlock() {
        return block;
    }

    public int getBlockHash() {
        return this.blockHash;
    }

    @Override
    public String toString() {
        return "REQUEST [FROM = " + this.getSource() + ", TO = " + this.getDestination() + ", TYPE = " + this.getType() + ", BLOCKHASH = " + blockHash + "]";
    }
}
