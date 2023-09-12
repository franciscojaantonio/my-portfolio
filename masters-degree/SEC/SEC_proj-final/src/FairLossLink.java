package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class FairLossLink {
	private DatagramSocket socket = null;

    public FairLossLink(int localPort) {
        try {
            this.socket = new DatagramSocket(localPort);
        } catch (SocketException e) {
            System.err.println("[Fair Loss Link] Unable to create socket in port " + localPort + "!");
            return;
        }
    }

    public void sendPacket(InetAddress address, int port, byte[] msgBytes) {
        DatagramPacket dataPacket = new DatagramPacket(msgBytes, msgBytes.length, address, port);
        try {
            socket.send(dataPacket);
        } catch (IOException e) {
            System.err.println("[Fair Loss Link] Error sending the packet!");
        }
    }

    public byte[] receivePacket() {
        byte[] buffer = new byte[10240];
        DatagramPacket dataPacket = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(dataPacket);
        } catch (IOException e) {
            System.err.println("[Fair Loss Link] Error receiving the packet!");
        }
        return buffer;
    }
}
