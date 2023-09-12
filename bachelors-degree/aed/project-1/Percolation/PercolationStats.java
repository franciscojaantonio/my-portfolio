import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

public class PercolationStats {
	private int N;
	private int trials;
	private double [] results;
	
	// perform independent trials on an n-by-n grid
    public PercolationStats(int n, int trials) {
    	if (n<=0)
    		throw new IllegalArgumentException("Invalid Arguments");
    	if (trials<=0)
    		throw new IllegalArgumentException("Invalid Arguments");
    	this.N=n;
    	this.trials=trials;
    	this.results=new double[trials];
    	for (int i=0; i<trials; i++)
    		results[i]=percolationThreshold();
    }

    // sample mean of percolation threshold
    public double mean() {
    	return StdStats.mean(results);
    }

    // sample standard deviation of percolation threshold
    public double stddev() {
    	if (trials == 1)
    		throw new IllegalArgumentException("Undefined Result");
    	return StdStats.stddev(results);
    }

    // low endpoint of 95% confidence interval
    public double confidenceLo() {
    	return mean() - 1.96*stddev()/Math.sqrt(trials);
    }

    // high endpoint of 95% confidence interval
    public double confidenceHi() {
    	return mean() + 1.96*stddev()/Math.sqrt(trials);
    }
 
    private double percolationThreshold() {
    	Percolation percolation = new Percolation(N);
    	int row;
    	int col;
    	int cont=0;
    	while(!percolation.percolates()) {
    		row=StdRandom.uniform(N)+1;
    		col=StdRandom.uniform(N)+1;
    		if (!percolation.isOpen(row, col)) {
    			cont++;
    			percolation.open(row, col);
    		}
    	}
    	return cont/Math.pow(N, (int) 2);
    }
    
    public static void main(String[] args) {
    }
}
  
