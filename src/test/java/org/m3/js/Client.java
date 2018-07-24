package org.m3.js;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 *
 */
public class Client {

    InetSocketAddress hostAddress;
    SocketChannel client;


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

        ByteBuffer buffer = ByteBuffer.allocate(74);
        buffer.put(message.getBytes());
        buffer.flip();
        client.write(buffer);
        buffer.clear();
    }


    public void placeOrder{

    }

    public void createFixMsg(){

        Map fixMsg = new HashMap<>();


        // Standard header

        

        // Required fields
        fixMsg.put("1");    // Account
        fixMsg.put("11");   // Client Order ID
        fixMsg.put("48");   // Security ID
        fixMsg.put("55");   // Symbol
        fixMsg.put("207");  // Security Exchange
        fixMsg.put("167");  // Security Type
        fixMsg.put("54");   // Side
        fixMsg.put("38");   // Order Quantity
        fixMsg.put("40");   // Order Type
        fixMsg.put("59");   // Time in Force
        fixMsg.put("60");   // Transaction Time
        fixMsg.put("21");   // HandlInst

        // Standard trailer








    }


}