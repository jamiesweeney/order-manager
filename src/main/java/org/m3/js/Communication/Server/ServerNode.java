package org.m3.js.Communication.Server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Defines the class used by a server to perform typical server functionality in
 * client/server communication.
 * Must be controlled using a ServerManager object
 */
public class ServerNode implements Runnable{

    // Constants
    private static final String EOF = "\0";
    private static final int BUFF_LEN = 1024;

    // ServerNode variables
    private Selector selector;
    private InetSocketAddress listenAddress;
    private ServerManager serverManager;

    // Misc variables
    private final Logger logger = Logger.getLogger(ServerNode.class);


    /**
     * Constructs a server node object
     * @param address the address to listen on
     * @param port the port to listen on
     * @param serverManager the server manager object
     */
    public ServerNode(String address, int port, ServerManager serverManager){
        listenAddress = new InetSocketAddress(address, port);
        this.serverManager = serverManager;
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
        this.setup();
        this.listen();
    }

    // ServerNode functionality methods
    /**
     * Sets up the server node object for use
     * @throws IOException
     */
    private void setup() throws IOException{
        // Create selector to handle multiple channels
        this.selector = Selector.open();

        // Create socket channel for accepting incoming connections
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        logger.info("Server started on >> " + this.listenAddress.toString());
    }

    /**
     * Listen for incoming messages
     * @throws IOException
     */
    public void listen(){
        // Wait for events
        while (true) {
            try {
                int readyCount = selector.select();
                if (readyCount == 0) {
                    continue;
                }

                // Iterate over all the keys
                Set<SelectionKey> keys = selector.selectedKeys();
                SelectionKey key;
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        // Accept client if new connection
                        this.acceptClient(key);

                    } else if (key.isReadable()) {
                        // Read from client and perform callback if incoming messages
                        String[] messages = this.readAllFromClient(key);
                        serverManager.readFromClientCallback(key, messages);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Accepts a new client
     * @param key the key that the client is connecting from
     * @throws IOException
     */
    private void acceptClient(SelectionKey key) throws IOException {
        // Get client address
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();

        // Accept client
        logger.info("New client connected to: " + remoteAddr);
        channel.register(this.selector, SelectionKey.OP_READ);

        // Perform callback
        serverManager.acceptClientCallback(remoteAddr);
    }

    /**
     * Read all messages from a client
     * @param key the key that the client is writing from
     * @throws IOException
     */
    private String[] readAllFromClient(SelectionKey key) throws IOException{
        // Read all incoming text and split multiple message up
        String[] messages = readFromClient(key).split(this.EOF);
        return messages;
    }

    /**
     * Read from a client
     * @param key the key that the client is writing from
     * @return
     * @throws IOException
     */
    private String readFromClient(SelectionKey key) throws IOException {
        // Get the socket channel to read from
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFF_LEN);
        int numRead = -1;
        numRead = channel.read(buffer);

        // If the client has disconnected then register this and callback
        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by server: " + remoteAddr);
            channel.close();
            key.cancel();
            this.serverManager.cancelClientCallback(remoteAddr);
            return null;
        }

        // Copy message data to byte array
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        // If length of data is the max length, continue reading
        if (numRead == BUFF_LEN){
            String nextStr = readFromClient(key);
            if (nextStr != null){
                return new String(data) + nextStr;
            }
        }
        return new String(data);
    }

    /**
     * Writes a message to a client
     * @param key the key that represents the client
     * @param message the message to write
     * @throws IOException
     */
    public void writeToClient(SelectionKey key, String message) throws IOException{
        // Add the EOF
        message = message + EOF;

        // Get channel to write
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer;

        // Write
        buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }
}
