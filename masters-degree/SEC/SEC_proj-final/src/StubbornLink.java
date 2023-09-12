package src;

import java.net.InetAddress;
import java.util.HashMap;

public class StubbornLink {
    // Uses
    private FairLossLink fairLossLink;

    // Senders
    private HashMap<String, Thread> senders = new HashMap<>();

    public StubbornLink(int localPort) {
        this.fairLossLink = new FairLossLink(localPort);
    }

    // Sends the message until confirmation
    public void send(InetAddress address, int port, Message msg) {
        byte[] data = MessageUtils.serialize(msg);

        Thread sending = new Thread(() -> {
            long timeout = 2;

            while(true) {
                try {
                    fairLossLink.sendPacket(address, port, data);

                    // Timeout until next send
                    Thread.sleep(timeout);

                    // Exponential growth 
                    timeout = Math.min((long) Math.exp(timeout), Long.MAX_VALUE);

                } catch (InterruptedException e) {
                    //System.out.println("[Stubborn Link] " + msg + " sent " + counter + " times!");
                }
            }
        });
        // Keep sending the message in background
        senders.put(msg.getId(), sending);
        sending.start();
    }

    public Message receive() {
        byte[] data = fairLossLink.receivePacket();
        return MessageUtils.deserialize(data);
    }

    public void stopSender(String messageId) {
        Thread sender = senders.remove(messageId);

        if (sender == null)
            return;
            
        sender.interrupt();
    }

    public void acknowledge(Message msg) {
        msg.acknowledge();
        fairLossLink.sendPacket(msg.getSrcAddress(), msg.getSrcPort(), MessageUtils.serialize(msg));
    }
}
