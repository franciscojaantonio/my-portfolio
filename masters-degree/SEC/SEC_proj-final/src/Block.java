package src;

import java.io.Serializable;
import java.util.ArrayList;


public class Block implements Serializable {
    // Number of transactions needed before adding block to the blockchain
    private int MAX_TRANSACTION_NUMBER = 2;
    
    ArrayList<RequestCommand> transactions = new ArrayList<RequestCommand>();
    ArrayList<String> strTransactions = new ArrayList<String>();
    private BlockHeader blockHeader;
    private int size;
    

    public Block(int size){
        this.size = size;
    }

    public BlockHeader getBlockHeader(){
        return this.blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader){
        this.blockHeader = blockHeader;
    }

    public int getSize(){
        return this.size;
    }

    public ArrayList<RequestCommand> getTransactions(){
        return this.transactions;
    }

    public ArrayList<String> getStrTransactions(){
        return this.strTransactions;
    }

    public void addTransaction(RequestCommand transaction){
        transactions.add(transaction);
        strTransactions.add(transaction.toString());
        this.size ++;
    }

    public boolean isReadyToAdd(){
        return this.size == MAX_TRANSACTION_NUMBER;
    }
    
}
