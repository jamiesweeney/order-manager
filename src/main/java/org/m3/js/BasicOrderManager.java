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
 * Implements a basic order manager
 *     Accepts client connections
 *
 */
public class BasicOrderManager implements Runnable{

    private Selector selector;
    private InetSocketAddress listenAddress;

    private String address;
    private int port;


    /**
     * Constructs an instance of the BasicOrderManager class
     *
     * @param address the hostname to listen for client connections
     * @param port the port number to listen for client connections
     * @throws IOException
     */
    public BasicOrderManager(String address, int port) throws IOException {
        this.address = address;
        this.port = port;

        listenAddress = new InetSocketAddress(address, port);
    }


    /**
     * Starts the Order Manager server and processes orders
     *
     * @throws IOException
     */
    void start() throws IOException {

        // Create selector to handle multiple channels
        this.selector = Selector.open();

        // Create socket channel for accepting incoming connections
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port >> " + this.port);

        this.listen();
    }


    void stop(){

    }


    /**
     * Listens for new incoming client messages
     *
     * @throws IOException
     */
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
                    this.write(key);
                }
            }
        }
    }


    /**
     * Accepts a new client connection
     *
     * @param key
     * @throws IOException
     */
    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);
        channel.register(this.selector, SelectionKey.OP_READ);
    }


    /**
     * Reads from the client connection key
     *
     * @param key
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);


        System.out.println(channel.socket().getRemoteSocketAddress().toString());

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));
    }


    /**
     * @param key
     */
    private void write(SelectionKey key){
       return;
    }


    @Override
    public void run() {
        try {
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}