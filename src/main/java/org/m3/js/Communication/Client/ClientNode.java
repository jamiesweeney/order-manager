package org.m3.js.Communication.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientNode {

    InetSocketAddress hostAddress;
    SocketChannel server;

    ClientManager clientManager;

    public ClientNode(ClientManager clientManager){
        this.clientManager = clientManager;
    }

    public void connect(String hostname, int port) throws IOException {
        hostAddress = new InetSocketAddress(hostname, port);
        server = SocketChannel.open(hostAddress);
    }

    public void disconnect() throws IOException {
        server.close();
    }

    public void writeToServer(String message) throws IOException {
        // Add EOF to message
        message = message + "\0";

        // Send message
        ByteBuffer buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        server.write(buffer);
        buffer.clear();
    }

    public String readFromServer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        int numRead = -1;
        numRead = server.read(buffer);

        if (numRead == -1) {
            Socket socket = server.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by server: " + remoteAddr);
            server.close();
            return null;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        return new String(data);
    }

    public void listen() {
        while (true){
            try {
                String message = this.readFromServer();
                this.clientManager.readFromServerCallback(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
