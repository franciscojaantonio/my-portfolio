import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
	private int size;
	private int side;
	private boolean [] grid;
	private WeightedQuickUnionUF wqu1;
	private WeightedQuickUnionUF wqu2;
	private int virtualTop;
	private int virtualBottom;
	private int openSites;
	
	// creates n-by-n grid, with all sites initially blocked
    public Percolation(int n) {
    	if (n<=0)
    		throw new IllegalArgumentException("Only n>0!"); 
    	this.size=n*n;
    	this.side=n;
    	this.grid= new boolean [size];
    	this.wqu1 = new WeightedQuickUnionUF (size+2);
    	this.wqu2 = new WeightedQuickUnionUF (size+1);
    	this.virtualBottom=size+1;
    	this.virtualTop=size;
    	this.openSites=0;
    }	   

    // opens the site (row, col) if it is not open already
    public void open(int row, int col) {
    	isValid(row, col);
    	if (!isOpen(row, col)) {    	
    		int vecPos=getPointInVec(row, col);    	
    		grid[vecPos]=true;    	
    		connectOpenNeighbors(row, col);
    		openSites+=1;
    	}
    }
    
    // is the site (row, col) open?
    public boolean isOpen(int row, int col) {
    	isValid(row,col);
    	int vecPos=getPointInVec(row,col);
    	return (grid[vecPos]==true);
    }

    // is the site (row, col) full?
    public boolean isFull(int row, int col) {
    	isValid(row, col);
    	if (!isOpen(row,col))
    		return false;
    	return wqu2.find(virtualTop)==wqu2.find(getPointInVec(row,col));
    }

    // returns the number of open sites
    public int numberOfOpenSites() {
    	return openSites;
    }
    
    		
    // does the system percolate?
    public boolean percolates() {
    	return ( wqu1.find(virtualTop) == wqu1.find(virtualBottom) );
    }
    
    private void isValid (int row, int col) {
    	if (row<=0 || row>side)
    		throw new IllegalArgumentException("Invalid Arguments");
    	if (col>side || col<=0)
    		throw new IllegalArgumentException("Invalid Arguments");
    }
    
    private int getPointInVec (int row, int col) {
    	int vecPos=(((row*side)-side)+col)-1; 
    	return vecPos;
    }
    
    private void connectOpenNeighbors(int row, int col) {
    	int vecPos=getPointInVec(row, col);
    	if (row<side)
    		setUnion(row+1, col, vecPos);
    	else
    		wqu1.union(vecPos, virtualBottom);
    	if (row>1)
    		setUnion(row-1, col, vecPos);
    	else {
    		wqu1.union(vecPos, virtualTop);
    		wqu2.union(vecPos, virtualTop);
    	}
    	if (col<side) 
    		setUnion(row, col+1, vecPos);
        if (col>1) 
        	setUnion(row, col-1, vecPos);
    	
    }
    
    private void setUnion (int row, int col, int index) {
    	if (isOpen(row, col)) {
    		wqu1.union(getPointInVec(row , col), index);
    		wqu2.union(getPointInVec(row , col), index);
    	}
    }
    
    public static void main(String[] args) {
    }
}
    
    	
    	
    