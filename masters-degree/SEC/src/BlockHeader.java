package src;

import java.io.Serializable;

public class BlockHeader implements Serializable {
    private Integer proposer;
    private Integer prevHeaderHash;
    private long timestamp;

    public BlockHeader(Integer proposer, Integer prevHeaderHash, long timestamp){
        this.proposer = proposer;
        this.prevHeaderHash = prevHeaderHash;
        this.timestamp = timestamp;
    }

    public Integer getProposer(){
        return this.proposer;
    }

    public Integer getprevHeaderHash(){
        return this.prevHeaderHash;
    }

    public long gettimestamp(){
        return this.timestamp;
    }
}
