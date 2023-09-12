package src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MessageUtils {

    public static byte[] serialize(Message message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(message);
            return bos.toByteArray();
        } catch (IOException e) {
            System.err.println("Could not serialize the object!");
        }
        return null;
    }

    public static Message deserialize(byte[] data) {
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(data);
            ObjectInputStream oi = new ObjectInputStream(bi);

            Message message = (Message) oi.readObject();
            return message;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not deserialize the data!");
            e.printStackTrace();
        }
        return null;
    }
}
