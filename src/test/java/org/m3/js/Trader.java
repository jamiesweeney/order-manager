package org.m3.js;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


/**
 *
 * This is a simple NIO based server.
 *
 */
public class Trader implements Runnable{

    private Selector selector;
    private InetSocketAddress listenAddress;

    private String address;
    private int port;


    /**
     *
     * @param address
     * @param port
     * @throws IOException
     */
    public Trader(String address, int port) throws IOException {

        this.address = address;
        this.port = port;

        listenAddress = new InetSocketAddress(address, port);
    }



    void startServer() throws IOException {


        // Create selector to handle multiple channels
        this.selector = Selector.open();

        // Create socket channel for accepting incoming connections
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Trader Server started on port >> " + this.port);

        this.listen();
    }


    private void listen() throws IOException {

        // Wait for events
        while (true) {
            int readyCount = selector.select();

            if (readyCount == 0) {
                continue;
            }

            // Iterate over all the keys
            Set<SelectionKey> keys = selector.selectedKeys();
            SelectionKey key;
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()){

                key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.acceptClient(key);
                } else if (key.isReadable()) {
                    this.read(key);
                } else if (key.isWritable()) {

                }
            }
        }
    }


    // accept server connection
    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);
        channel.register(this.selector, SelectionKey.OP_READ);
    }


    // read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);


        System.out.println(channel.socket().getRemoteSocketAddress().toString());

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by server: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));
    }


    @Override
    public void run() {
        try {
            this.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}