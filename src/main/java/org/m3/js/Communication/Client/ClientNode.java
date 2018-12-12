package org.m3.js.Communication.Client;

import org.apache.log4j.Logger;
import org.m3.js.Communication.Server.ServerNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Defines the class used by a server to perform typical server functionality in
 * client/server communication.
 * Must be controlled using a ClientManager object
 */
public class ClientNode implements Runnable{

    // Constants
    private static final String EOF = "\0";
    private static final int BUFF_LEN = 1024;

    // ClientNode variables
    private ClientManager clientManager;
    private InetSocketAddress hostAddress;
    private SocketChannel server;

    /**
     * Constructs a client node object
     * @param address the address to connect to
     * @param port the port to connect to
     * @param clientManager the client manager object
     */
    public ClientNode(String address, int port, ClientManager clientManager){
        this.clientManager = clientManager;
        this.hostAddress = new InetSocketAddress(address, port);
    }


    // Thread override methods
    /**
     * Provides a method for implementing the Runnable Class
     */
    @Override
    public void run() {
        try {
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Control methods
    /**
     * Starts the server
     * @throws IOException
     */
    private void start() throws IOException {
        this.listen();
    }


    // ClientNode functionality methods
    /**
     * Connects to the server
     * @throws IOException
     */
    public void connect() throws IOException {
        // Open channel and perform callback
        server = SocketChannel.open(hostAddress);
        this.clientManager.acceptedByServerCallback(hostAddress);
    }

    /**
     * Disconnects from the server
     * @throws IOException
     */
    public void disconnect() throws IOException {
        // Close channel and perform callback
        server.close();
        this.clientManager.disconnectedFromServerCallback(hostAddress);
    }

    /**
     * Listen for incoming messages
     * @throws IOException
     */
    private void listen() {
        while (true){
            try {
                // Get message and perform callback
                String[] messages = this.readAllFromServer();
                this.clientManager.readFromServerCallback(messages);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads all messages from the server
     * @return
     * @throws IOException
     */
    public String[] readAllFromServer() throws IOException{
        // Read all incoming text and split by delim
        String[] messages = readFromServer().split(this.EOF);
        return messages;
    }

    /**
     * Reads from the server
     * @return
     * @throws IOException
     */
    private String readFromServer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFF_LEN);
        int numRead = -1;
        numRead = server.read(buffer);

        // If the server has disconnected then register and callback
        if (numRead == -1) {
            Socket socket = server.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by server: " + remoteAddr);
            server.close();
            return null;
        }

        // Copy message data to byte array
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        // If length of data is the max length, continue reading
        if (numRead == BUFF_LEN) {
            String nextStr = readFromServer();
            if (nextStr != null){
                return new String(data) + readFromServer();
            }
        }
        return new String(data);
    }

    /**
     * Writes a message to the server
     * @param message the message to write
     * @throws IOException
     */
    public void writeToServer(String message) throws IOException {
        // Add EOF to message
        message = message + EOF;

        // Send message
        ByteBuffer buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        server.write(buffer);
        buffer.clear();
    }
}
