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
import java.util.*;

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
    private boolean running;


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
     * Starts the Order Manager server.
     */
    public void start() {

        try {
            setup();
            this.running = true;
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Stops the Order manager server.
     */
    public void stop(){
        this.running = false;
    }


    /**
     * Sets up the order manager server connection channels
     * @throws IOException
     */
    private void setup() throws IOException {
        // Create selector to handle multiple channels
        this.selector = Selector.open();

        // Create socket channel for accepting incoming connections
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port >> " + this.port);
    }


    /**
     * Listens for new incoming client messages
     *
     * @throws IOException
     */
    private void listen() throws IOException {
        Set<SelectionKey> keys;
        SelectionKey key;
        Iterator<SelectionKey> iterator;

        // Wait for events
        while (this.running) {

            // Skip if nothing
            if (selector.select() == 0) {
                continue;
            }

            // Iterate over all the keys
            keys = selector.selectedKeys();
            iterator = keys.iterator();

            while (iterator.hasNext()){
                key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.acceptClient(key);
                } else if (key.isReadable()) {
                    this.readClient(key);
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




    private void readClient(SelectionKey key){

        // Get the message
        try {
            String message = this.read(key);

            // Return if the client has disconnected
            if (message == null){
                return;
            }

            if (this.isValidMessage(message)){


            }else{

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads from the client connection key
     *
     * @param key
     * @throws IOException
     */
    private String read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        System.out.println(numRead);

        // Check if the socket has been closed
        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return null;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        return new String(data);
    }

    private boolean isValidMessage(String message){

        int checkExpected = -1;
        int sum = 0;

        // Split into tags
        String[] splitMsg = message.split("\\|");
        final LinkedHashMap tags = new LinkedHashMap<Integer, String>();
        for (String s : splitMsg){
            String[] elems = s.split("=");
            tags.put(Integer.parseInt(elems[0]),elems[1]);

            if (Integer.parseInt(elems[0]) != 10){
                sum += sumASCII(s) + 124;
            }
        }

        // Check for all essential tags


        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(tags.entrySet());
        }};

        int lengthExpected = -1;
        int length = 0;


        // Iterate over the tags
        for (Map.Entry<Integer,String> entry : entries){

            // Skip first 2 tags and stop at the checksum
            if (entry.getKey().equals(8)){
                continue;
            }else if (entry.getKey().equals(9)){
                lengthExpected = Integer.parseInt(entry.getValue());
                continue;
            }else if (entry.getKey().equals(10)){
                checkExpected = Integer.parseInt(entry.getValue());
                break;
            }

            // Add key=value|
            length += entry.getKey().toString().length();
            length += 1;
            length += entry.getValue().length();
            length += 1;
        }

        int check = sum%256;
        boolean lengthEqual = (length == lengthExpected);
        boolean checkEqual = (check == checkExpected);



        return (lengthEqual && checkEqual);
    }




    /**
     * @param key
     */
    private void write(SelectionKey key, String message) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer;

        // Write
        buffer = ByteBuffer.allocate(74);
        buffer.put(message.getBytes());
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }


    /**
     * Provides a method for implementing the Runnable Class
     */
    @Override
    public void run(){
        this.start();
    }

    private int sumASCII(String str) {
        int sum = 0;
        for (char c : str.toCharArray()) {
            sum += (int) c;
        }
        return sum;
    }


}