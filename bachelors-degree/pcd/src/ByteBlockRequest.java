import java.io.Serializable;

public class ByteBlockRequest implements Serializable {
    private final int startIndex;
    private final int length;

    public ByteBlockRequest(int startIndex, int length){
        this.startIndex = startIndex;
        this.length = length;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getLength() {
        return length;
    }
}
