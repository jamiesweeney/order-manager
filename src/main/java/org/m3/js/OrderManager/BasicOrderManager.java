package org.m3.js.OrderManager;

import org.apache.log4j.Logger;
import org.m3.js.Messages.*;
import org.m3.js.Messages.ReportMessages.ExecutionReportMessage;
import org.m3.js.Messages.ReportMessages.RejectMessage;
import org.m3.js.Orders.MarketOrder;

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

    // Constants
    private static final String EOF = "\0";
    private static final String DELIM = "|";

    // Varaibles for I/O
    private Selector selector;
    private InetSocketAddress listenAddress;
    private String address;
    private int port;

    private int runStatus = 0;

    private String id;

    private int msgCount = 0;
    private int execMsgCount = 0;
    private long orderID = 0;

    private Map<String, String> clients;
    private int clientCount = 0;

    private Socket trader;

    Map<Long,MarketOrder> orders;

    private Logger logger = Logger.getLogger(BasicOrderManager.class);


    /**
     * Constructs an instance of the BasicOrderManager class
     *
     * @param address the hostname to listen for client connections
     * @param port the port number to listen for client connections
     * @throws IOException
     */
    public BasicOrderManager(String address, int port, String id){
        this.address = address;
        this.port = port;
        this.id = id;

        listenAddress = new InetSocketAddress(address, port);

        clients = new HashMap<>();

        orders = new HashMap<>();

        logger.info("BasicOrderManager created: address="+address + " port=" + port + " id=" + id);
    }


    /**
     * Starts the Order Manager server.
     */
    public void start() {
        try{
            switch (this.runStatus){

                // Never ran before
                case 0:
                    logger.info("BasicOrderManager starting");
                    setup();
                    break;

                // Running
                case 1:
                    logger.error("BasicOrderManager already running");
                    return;

                // Paused
                default:
                    logger.info("BasicOrderManager is being unpaused");
                    break;
            }
            this.runStatus = 1;
            listen();
        } catch (IOException e) {
            logger.error("BasicOrderManager threw an IOError during startup");
            e.printStackTrace();
        }
    }


    /**
     * Stops the Order manager server.
     */
    public void pause(){
        switch (this.runStatus){

            // Never ran before
            case 0:
                logger.error("BasicOrderManager is not running");
                return;

            // Running
            case 1:
                logger.info("Pausing the BasicOrderManager");
                this.runStatus = 2;
                return;

            // Paused
            default:
                logger.error("BasicOrderManager is already paused");
                return;
        }
    }


    /**
     * Sets up the order manager server connection channels
     * @throws IOException
     */
    private void setup() throws IOException {


        setupClientServer();
        setupTraderConnection();

    }

    private void setupClientServer() throws IOException{
        // Create selector to handle multiple channels
        this.selector = Selector.open();

        // Create socket channel for accepting incoming connections
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind server socket channel to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        logger.info("Server started on port " + this.port);
    }

    private void setupTraderConnection() throws IOException {
        InetSocketAddress hostAddress = new InetSocketAddress(this.address, 1000);
        SocketChannel server = SocketChannel.open(hostAddress);

        String message = "WORK PLEASE";
        // Send message
        ByteBuffer buffer = ByteBuffer.allocate(message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        server.write(buffer);
        buffer.clear();
    }


    /**
     * Listens for new incoming client messages
     *
     * @throws IOException
     */
    private void listen() throws IOException {
        logger.info("Server listening on port " + this.port);

        Set<SelectionKey> keys;
        SelectionKey key;
        Iterator<SelectionKey> iterator;

        // Wait for events
        while (this.runStatus == 1) {

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

        // Accept client
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        channel.register(this.selector, SelectionKey.OP_READ);

        // Register client in OM
        String clientName = "client"+this.clientCount;
        this.clients.put(remoteAddr.toString(), clientName);
        logger.info(clientName + " connected on " + remoteAddr.toString());
    }

    private void serveID(SelectionKey key, String clientName){

        // Send OM id and client id to the client
        String writeMsg = clientName + this.DELIM + this.id;
        try {
            this.write(key, writeMsg);
        } catch (IOException e) {
            logger.error("IOException while serving ID to " + clientName);
            logger.error(e.getStackTrace());
        }
    }


    private void cancelClient(SocketChannel channel, SelectionKey key) throws IOException {

        // Unregister connection
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        channel.close();
        key.cancel();

        logger.info(this.clients.get(remoteAddr.toString())+ " disconnected from " + remoteAddr.toString());

        // Unregister client
        this.clients.remove(remoteAddr.toString());
    }


    /**
     * Handles a new client message
     *
     * @param key
     */
    private void readClient(SelectionKey key){

        // Get the client address
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        String clientName = this.clients.get(remoteAddr.toString());

        // Attempt to get the messages
        try {
            String[] messages = this.readAll(key);

            // Iterate over messages
            for (String message : messages){
                logger.debug("New message from: "+ clientName + ": " + message);

                // Deal with the message
                switch (message){

                    case "":
                        break;
                    case "ID_REQUEST":
                        this.serveID(key, clientName);
                        break;
                    default:
                        this.processMessage(message, key, clientName);
                }
            }
        } catch (IOException e) {
            logger.error("IOException reading messages from " + clientName);
            logger.error(e.getStackTrace());

            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Reads from the client connection key
     *
     * @param key
     * @throws IOException
     */
    private String[] readAll(SelectionKey key) throws IOException {

        String all = this.read(key);
        if (all == null){
            return null;
        }
        return all.split(this.EOF);
    }

    private String read(SelectionKey key) throws IOException{

        // Setup channel and  buffer
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);

        // If the client has disconnected
        if (numRead == -1) {
            // Unregister and disconnect if client has closed connection
            this.cancelClient(channel, key);
            return "";
        }

        // Read the message
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        // Call recursively if still some message left
        if (numRead < 1024){
            return new String(data);
        }else{
            return new String(data) + this.read(key);
        }
    }

    private void processMessage(String message, SelectionKey key, String clientName) {

        Message msg;
        int seqNum = -1;


        // Try get the sequence number
        String[] tags = message.split("\\|");


        for (String s : tags){
            if (s.startsWith("34=")){
                seqNum = Integer.parseInt(s.substring(3));
            }
        }

        // Create failed message or parse message from string
        if (seqNum == -1){
            msg = new FailedMessage("FIX-EX: Could not get the message sequence number.");
        }else{
            msg = Message.parseFromText(message);
        }

        if (msg.getClass().equals(FailedMessage.class)){

            // If the message has failed, send rejection message
            try {
                RejectMessage rejMsg = new RejectMessage();
                rejMsg.addHeader("Fix.4.4", this.id, clientName, this.msgCount++);
                rejMsg.addBody(seqNum,((FailedMessage) msg).getText());
                rejMsg.addTrailer();
                rejMsg.packageMessage();
                this.write(key, rejMsg.getMessageString());
            } catch (FixException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (msg.getClass().equals(NewOrderSingleMessage.class)){

            // Create a new order object
            Map<Integer, String> msgTags = ((NewOrderSingleMessage) msg).getTags();
            MarketOrder order = new MarketOrder(this.orderID++, msgTags.get(49), msgTags.get(11), msgTags.get(55), Integer.parseInt(msgTags.get(54)), Integer.parseInt(msgTags.get(38)));
            this.orders.put(order.getID(), order);

            // Send a confirmation to the client
            ExecutionReportMessage reportMessage = new ExecutionReportMessage();
            try {
                reportMessage.addHeader("Fix.4.4", this.id, clientName, this.msgCount++);
                reportMessage.addBody(order.getID(), order.getClOrdID(), this.execMsgCount++, '0',
                        order.getOrdStatus(), order.getOrdStatus(), order.getSymbol(), order.getSide(), order.getQuantity(),
                        order.getQuantity() - order.getCumQuantity(), order.getCumQuantity(), 0);
                reportMessage.addTrailer();
                reportMessage.packageMessage();
                this.write(key, reportMessage.getMessageString());
            } catch (FixException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Send the order to the trader



        }
    }


    /**
     * @param key
     */
    private void write(SelectionKey key, String message) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        ByteBuffer buffer;

        // Add EOF to message
        message = message + this.EOF;

        logger.debug("Sending message \"" + message + "\" to " + remoteAddr);

        // Check that client connection is still open
        if (!this.clients.containsKey(remoteAddr.toString())){
            logger.error("Client previously on address " + remoteAddr + " has disconnected, cannot send message");
            return;
        }

        // Write
        buffer = ByteBuffer.allocate(message.length());
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

}