import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.princeton.cs.algs4.StdRandom;

public class RandomizedQueue<Item> implements Iterable<Item> {
	private int size;
	private Item[] items; 
	// construct an empty randomized queue
	public RandomizedQueue() {
		size=0;
		items=(Item[])new Object[1];		
	} 
	// is the randomized queue empty? 
	public boolean isEmpty() { 
		return size==0;		
	} 
	// return the number of items on the randomized queue 
	public int size() {
		return size;
	} 
	//
	private void resize (int capacity) {
		Item [] copy= (Item[]) new Object[capacity];
		for (int i=0; i<size; i++)
			copy[i]=items[i];
		items=copy;
	}
	
	// add the item
	public void enqueue(Item item) {
		if (item==null)
			throw new IllegalArgumentException();
		if (size==items.length)
			resize (2*items.length);
		if (size>0) {
			int index=StdRandom.uniform(size);
			Item copy=items[index];
			items[index]=item;
			items[size++]=copy;			
		}
		else
			items[size++]=item;	
	}
	
	// remove and return a random item
	public Item dequeue() {
		if (isEmpty())
			throw new NoSuchElementException();
		if (size == (items.length/4))
			resize (items.length/2);
		int index=StdRandom.uniform(size);
		Item item=items[index];
		items[index]=items[--size];
		items[size]=null;
		return item;
	}
	
	// return a random item (but do not remove it) 
	public Item sample() {
		if (isEmpty())
			throw new NoSuchElementException();
		return items[StdRandom.uniform(size)];		
	} 
	//Generates a int [] 
	private int [] getVectorRandomIndex() {
		int [] r = new int [size];
		for (int j=0; j<size; j++)
			r[j]=j;
		StdRandom.shuffle(r);
		return r;		
	}
	
	// return an independent iterator over items in random order
	public Iterator<Item> iterator(){
		return new ListIterator();
	}		
		
	private class ListIterator implements Iterator<Item> {
		private int index;
		private int [] randomIndex;
		
		public ListIterator() {
			index=0;
			randomIndex=getVectorRandomIndex();		
		}
		@Override
		public boolean hasNext() {
			return index<size;
		}
		@Override
		public Item next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return items[randomIndex[index++]];
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}					
	} 
	// testing (required) 
	public static void main(String[] args) {
		RandomizedQueue<Integer> queue = new RandomizedQueue<>();
        for (int i = 0; i < 25; i++) {
            queue.enqueue(i);                     
        }
        queue.isEmpty();
        System.out.println(queue.size());
        queue.dequeue();
        queue.dequeue();
        System.out.println(queue.size());
        System.out.println(queue.sample());
	}
}