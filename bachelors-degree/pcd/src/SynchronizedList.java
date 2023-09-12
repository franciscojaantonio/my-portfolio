import java.util.LinkedList;

// Lista genérica e sincronizada
public class SynchronizedList<T> {
    private LinkedList<T> data;

    public SynchronizedList(){
        data = new LinkedList<>();
    }

    public T getFirst() {
        return data.getFirst();
    }

    public synchronized void removeObject(T item){
        data.remove(item);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public synchronized void add(T item) {
        data.add(item);
    }

    public synchronized T removeFirst() {
        if(data.isEmpty())
            return null;
        return data.removeFirst();
    }

    public boolean containsOrEmpty(T item) {
        if(data.isEmpty())
            return true;
        return data.contains(item);
    }

    public synchronized boolean addIfEmptyOrNotContains(T item) {
        if(isEmpty() || !contains(item)) {
            add(item);
            return true;
        }
        return false;
    }

    public LinkedList<T> getList() {
        return data;
    }

    public boolean contains(T item) {
        return data.contains(item);
    }

    @Override
    public String toString() {
        String res = "[";
        for(T item: data)
            res += item;
        return res.concat("]");
    }
}
