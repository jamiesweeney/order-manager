package org.m3.js;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class Client {

    InetSocketAddress hostAddress;
    SocketChannel client;


    private String fixVersion = "FIX.4.4";
    private String accountID = "TestAccount";
    private int msgCount = 0;

    public void connect(String hostname, int port) throws IOException, InterruptedException {
        hostAddress = new InetSocketAddress(hostname, port);
        client = SocketChannel.open(hostAddress);
    }

    public void disconnect() throws IOException {
        client.close();
    }

    public void writeMany(String[] messages) throws IOException {

        for (String message : messages){
            this.write(message);
        }
    }

    public void write(String message) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        client.write(buffer);
        buffer.clear();
    }

    public String read() throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = client.read(buffer);

        if (numRead == -1) {
            Socket socket = client.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by server: " + remoteAddr);
            client.close();
            return null;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        return new String(data);
    }


    public void placeNewMarketOrder(String symbol, int side, int quantity){
        try {
            FixBuilder fbuild = new FixBuilder();
            fbuild.addHeader("FIX.4.4", "D");
            fbuild.addBody(this.accountID, this.msgCount, symbol, side, quantity);
            fbuild.addFooter();
            String message = fbuild.getMessageString();
            this.write(message);

            // Only want to add one if successful
            this.msgCount++;
        } catch (FixException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
