package org.m3.js;

import org.m3.js.Messages.FixException;
import org.m3.js.Messages.NewOrderSingleMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class Client {

    InetSocketAddress hostAddress;
    SocketChannel server;

    private String fixVersion;
    private int msgCount = 0;

    private String clientID;
    private String OMID;


    public Client(String fixVersion){
        this.fixVersion = fixVersion;
    }

    public void connect(String hostname, int port) throws IOException{
        hostAddress = new InetSocketAddress(hostname, port);
        server = SocketChannel.open(hostAddress);

        // Get the id for the client and the server
        this.write("ID_REQUEST");
        String resp = this.readAll()[0];
        String[] ids = resp.split("\\|");

        // Set ids
        this.clientID = ids[0];
        this.OMID = ids[1];
    }

    public void disconnect() throws IOException {
        server.close();
    }

    public void writeMany(String[] messages) throws IOException {

        for (String message : messages){
            this.write(message);
        }
    }

    public void write(String message) throws IOException {

        // Add EOF to message
        message = message + "\0";

        // Send message
        ByteBuffer buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        server.write(buffer);
        buffer.clear();
    }

    public String[] readAll() throws IOException {

        String[] messages = this.read().split("\0");
        return messages;
    }

    public String read() throws IOException {

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



    public void placeNewMarketOrder(String symbol, int side, int quantity){
        try {

            int newID = this.msgCount++;
            String clOrdID = this.clientID + "_" + newID;
            NewOrderSingleMessage nosm = new NewOrderSingleMessage();
            nosm.addHeader(this.fixVersion, this.clientID, this.OMID, newID);
            nosm.addBody(clOrdID, symbol, side, quantity, "1");
            nosm.addTrailer();
            nosm.packageMessage();
            String message = nosm.getMessageString();
            this.write(message);

            // Only want to add one if successful
        } catch (FixException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        while (true){
            try {
                String[] messages = this.readAll();
                for (String message : messages){
                    System.out.println("Client got: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
