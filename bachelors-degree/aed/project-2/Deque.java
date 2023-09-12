import java.util.Iterator;
import java.util.NoSuchElementException;

public class Deque<Item> implements Iterable<Item> {
	private Node first;
	private Node last;
	private int size;
 
	// construct an empty deque
	public Deque() {
		size=0;
	}
 
	// is the deque empty?
	public boolean isEmpty() {
		return size==0;
	}
 
	// return the number of items on the deque 
	public int size() {
		return size;
	}
 
	// add the item to the front
	public void addFirst(Item item) {
		if (item==null)
			throw new IllegalArgumentException();
		Node oldFirst=first;
		first=new Node();
		first.item=item;
		first.next=oldFirst;
		first.prev=null;
		if (isEmpty())
			last=first;
		else 
			oldFirst.prev=first;
		size++;
	}

	// add the item to the back
	public void addLast(Item item) {
		if (item==null)
			throw new IllegalArgumentException();
		Node oldLast=last;
		last=new Node();
		last.item=item;
		last.next=null;
		last.prev=oldLast;
		if (isEmpty())
			first=last;
		else
			oldLast.next=last;
		size++;
	}

	// remove and return the item from the front
	public Item removeFirst() {
		if (isEmpty())
			throw new NoSuchElementException();
		size--;
		Item item=first.item;
		if (isEmpty()) {
			first=null;
			last=null;
		}
		else {
			first=first.next;
			first.prev=null;
		}
		return item;
	}
 
	// remove and return the item from the back
	public Item removeLast() {
		if (isEmpty())
			throw new NoSuchElementException();
		size--;
		Item item=last.item;
		if (isEmpty()) {
			last=null;
			first=null;
		}
		else {
			last=last.prev;
			last.next=null;
		}
		return item;
	}
 
	// return an iterator over items in order from front to back
	public Iterator<Item> iterator(){
		return new ListIterator();
	}
	
	private class ListIterator implements Iterator<Item> {
		private Node current=first;

		@Override
		public boolean hasNext() {
			return current!=null;
		}
		public void remove() {
			throw new  UnsupportedOperationException();
		}

		@Override
		public Item next() {
			if (!hasNext())
				throw new NoSuchElementException();
			else {
				Item item=current.item;
				current=current.next;
				return item;
			}			
		}		
	}
	
	private class Node {	
		Item item;	 
		Node prev;
		Node next;  
	}	
 
	// testing (required)
	public static void main(String[] args) {
		 Deque<Integer> deque = new Deque<>();
	        deque.addFirst(1);
	        deque.addLast(2);
	        System.out.println(deque.size());
	        deque.removeFirst();
	        deque.removeLast();
	        System.out.println(deque.isEmpty());
	}
}
	