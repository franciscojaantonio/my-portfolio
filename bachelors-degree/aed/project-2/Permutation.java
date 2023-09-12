import edu.princeton.cs.algs4.StdIn;

public class Permutation {

	public static void main(String[] args) {
		int k,j=0;
		k=Integer.parseInt(args[0]);
        RandomizedQueue<String> RQueue = new RandomizedQueue<>();
        while (!StdIn.isEmpty())           
        	RQueue.enqueue(StdIn.readString());    
        while (j<k) {
            System.out.println(RQueue.dequeue());
            j++;
        }
    }
}
	
/* Francisco Antonio 92613
   Filipe Serra 92666      */
