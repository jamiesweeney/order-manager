package org.m3.js.Communication.Server;

import org.m3.js.Communication.Server.ServerManager;

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

public class ServerNode implements Runnable{

    private Selector selector;
    private InetSocketAddress listenAddress;

    private ServerManager manager;

    public ServerNode(String address, int port, ServerManager manager){
        listenAddress = new InetSocketAddress(address, port);

        this.manager = manager;
    }

    private void start() throws IOException {
        this.setup();
        this.listen();
    }

    private void setup() throws IOException{
        // Create selector to handle multiple channels
        this.selector = Selector.open();

        // Create socket channel for accepting incoming connections
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on >> " + this.listenAddress.toString());
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
                    String message = this.readFromClient(key);
                    if (message != null){
                        manager.readFromClientCallback(key, message);
                    }
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
        System.out.println("New client connected to: " + remoteAddr);
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    // read from the socket channel
    private String readFromClient(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by server: " + remoteAddr);
            channel.close();
            key.cancel();
            return null;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        return new String(data);
    }

    public void writeToClient(SelectionKey key, String message) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer;

        // Write
        buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
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
