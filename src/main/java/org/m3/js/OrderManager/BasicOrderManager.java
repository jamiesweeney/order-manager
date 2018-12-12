package org.m3.js.OrderManager;

import org.apache.log4j.Logger;
import org.m3.js.Communication.Client.ClientManager;
import org.m3.js.Communication.Client.ClientNode;
import org.m3.js.Communication.Server.ServerManager;
import org.m3.js.Communication.Server.ServerNode;
import org.m3.js.Messages.FailedMessage;
import org.m3.js.Messages.FixException;
import org.m3.js.Messages.Message;
import org.m3.js.Messages.NewOrderSingleMessage;
import org.m3.js.Messages.ReportMessages.ExecutionReportMessage;
import org.m3.js.Messages.ReportMessages.RejectMessage;
import org.m3.js.Orders.MarketOrder;
import org.m3.js.Orders.Order;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Implements a basic order manager
 *     Accepts client connections
 *
 */
public class BasicOrderManager implements Runnable, ServerManager, ClientManager {

    // Constants
    private static final String EOF = "\0";
    private static final String DELIM = "|";

    // OrderManager variables
    private String id;
    private int clientCount = 0;
    private int msgCount = 0;
    private int execMsgCount = 0;
    private long orderID = 0;

    // Server manager variables
    private ServerNode serverNode;
    private Thread serverListenThread;
    private Map<String, String> clients;

    // Client manager variables
    private ClientNode clientNode;
    private Thread clientListenThread;

    // Order variables
    private Map<Long,Order> orders;
    private Thread orderCrossThread;

    // Misc variables
    private Logger logger = Logger.getLogger(BasicOrderManager.class);


    /**
     * Constructs an instance of the BasicOrderManager class
     *
     * @param address the hostname to listen for client connections
     * @param port the port number to listen for client connections
     * @param traderAddress the hostname to connect to the trader
     * @param traderPort the port to connect to the trader
     * @param id the string that identifies the order manager to external components
     * @throws IOException
     */
    public BasicOrderManager(String address, int port, String traderAddress, int traderPort, String id){
        this.id = id;

        // Setup server node for client communication
        this.serverNode = new ServerNode(address, port, this);
        this.serverListenThread = new Thread(this.serverNode);

        // Setup client node for trader communication
        this.clientNode = new ClientNode(traderAddress, traderPort, this);
        try {
            this.clientNode.connect();
            this.clientListenThread = new Thread(this.clientNode);
        } catch (IOException e) {
            logger.error("Failed to connect to trader");
            e.printStackTrace();
        }

        clients = new HashMap<>();
        orders = new HashMap<>();

        logger.info("BasicOrderManager created: address="+address + " port=" + port + " id=" + id);
    }

    // Thread override methods
    /**
     * Provides a method for implementing the Runnable Class
     */
    @Override
    public void run(){
        this.start();
    }


    // Control methods
    /**
     * Starts the Order Manager server.
     */
    private void start() {
        this.serverListenThread.start();
        this.clientListenThread.start();
    }
    /**
     * Pauses the Order manager server.
     */
    public void pause(){
        this.serverListenThread.suspend();
        this.clientListenThread.suspend();

    }
    /**
     * Resumes the Order manager server.
     */
    public void resume(){
        this.serverListenThread.resume();
        this.clientListenThread.resume();
    }
    /**
     * Stops the Order manager server.
     */
    public void stop(){
        this.serverListenThread.stop();
        this.clientListenThread.stop();
    }


    // ServerManager callback methods
    /**
     * Callback method for accepting a new client
     * @param remoteAddr The address that the client is connecting from
     */
    public void acceptClientCallback(SocketAddress remoteAddr) {
        // Register client in OM
        String clientName = "client"+this.clientCount;
        this.clients.put(remoteAddr.toString(), clientName);
        logger.info(clientName + " connected on " + remoteAddr.toString());
    }

    /**
     * Callback method for accepting a new client
     * @param key The selection key that is used by the serverNode
     * @param messages The array of messages received
     */
    public void readFromClientCallback(SelectionKey key, String[] messages) {

        // Get the client address
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();

        // Get the client name using address
        String clientName = this.clients.get(remoteAddr.toString());

        // Iterate over messages
        for (String message : messages){
            logger.debug("New message from "+ clientName + ": " + message);

            // Deal with the message
            switch (message){

                case "ID_REQUEST":
                    this.serveID(key, clientName);
                    break;
                default:
                    this.processMessage(message, key, clientName);
            }
        }
    }

    /**
     * Callback method for disconnecting a client
     * @param remoteAddr The address that the client is disconnecting from
     */
    public void cancelClientCallback(SocketAddress remoteAddr){
        String clientName = this.clients.get(remoteAddr);
        this.clients.remove(remoteAddr);
        logger.info(clientName + " disconnected from " + remoteAddr.toString());
    }


    // ServerManger node control methods
    /**
     * Method for writing a message to the client
     */
    public void writeToClient(SelectionKey key, String message) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();

        // Add EOF to message
        message = message + this.EOF;

        logger.debug("Sending message \"" + message + "\" to " + remoteAddr);

        // Check that client connection is still open
        if (!this.clients.containsKey(remoteAddr.toString())){
            logger.error("Client previously on address " + remoteAddr + " has disconnected, cannot send message");
            return;
        }

        this.serverNode.writeToClient(key, message);
    }

    /**
     * Method for writing a message to a client, with only the client name
     */
    public void writeToSpecificClient(String clientName, String message){

        String address = (String) getKeyFromValue(this.clients, clientName);

        //TODO implement this
    }


    // ClientManager callback methods
    /**
     * Callback method for being accepted by a server
     * Right now only logs this information
     * @param remoteAddr the address of the server
     */
    public void acceptedByServerCallback(SocketAddress remoteAddr) {
        logger.info("Connected to trader on: " + remoteAddr.toString());
    }

    /**
     * Callback method for disconnecting from a server
     * Right now only logs this information
     * @param remoteAddr the address that the server is disconnecting from
     */
    public void disconnectedFromServerCallback(SocketAddress remoteAddr) {
        logger.info("Disconnected from trader on: " + remoteAddr.toString());
    }

    /**
     * Callback method for reading from a server
     * Takes a serialized object message and attempted to construct the
     * order object.
     * @param messages the array of messages received
     */
    public void readFromServerCallback(String[] messages) {
        // Iterate over messages
        for (String message : messages){
            logger.info("New message from trader: \"" + message + "\"");

            // Attempt to deserialize the message
            try {
                Object o = Order.deserialize(message);
                Order order;
                String className = o.getClass().getCanonicalName();

                // Determine the order type and cast to appropriate class
                switch (className) {
                    case "org.m3.js.Orders.MarketOrder":
                        order = (MarketOrder) o;
                        break;
                    default:
                        logger.error("Received an unsupported object type from trader");
                        return;
                }

                // Determine status of order
                switch (order.getOrdStatus()){

                    // Case of 'new' order
                    case '0':
                        // Send a acceptance confirmation to the client
                        this.orders.put(order.getId(), (Order)order);
                        try {
                            ExecutionReportMessage reportMessage = createExecutionReportMessage(order);
                            this.writeToSpecificClient(order.getClientID(), reportMessage.getMessageString());
                        } catch (FixException e) {
                            e.printStackTrace();
                        }
                        break;

                    // Case of 'rejected' order
                    case '8':
                        // Send a rejection message to the client
                        this.orders.remove(order.getId());
                        try {
                            ExecutionReportMessage reportMessage = createExecutionReportMessage(order);
                            this.writeToSpecificClient(order.getClientID(), reportMessage.getMessageString());
                        } catch (FixException e) {
                            e.printStackTrace();
                        }
                        break;

                    default:
                        logger.error("Unsupported order status");
                        return;
                }

                // TODO Update order list

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    // ClientManager node control methods
    /**
     * Writes a message to the server
     * @param message the message to write
     * @throws IOException
     */
    public void writeToServer(String message) throws IOException {
        logger.info("Sending message \"" + message + "\"");
        this.clientNode.writeToServer(message);
    }


    // OM logic methods for client/OM communication
    /**
     * Serves the OM ID and the client ID to the client when requested, for authentication
     * @param key the key that represents the client
     * @param clientName the name of the client
     */
    private void serveID(SelectionKey key, String clientName){

        // Send OM id and client id to the client
        String writeMsg = clientName + this.DELIM + this.id;
        try {
            this.writeToClient(key, writeMsg);
        } catch (IOException e) {
            logger.error("IOException while serving ID to " + clientName);
            logger.error(e.getStackTrace());
        }
    }

    /**
     * Processes a new client message
     *
     * @param message the message text
     * @param key the key representing the client connection
     * @param clientName the client name
     */
    private void processMessage(String message, SelectionKey key, String clientName) {
        Message msg;
        int seqNum = -1;

        // Try get the sequence number
        String[] tags = message.split("\\|");
        for (String s : tags){
            if (s.startsWith("34=")){
                try {
                    seqNum = Integer.parseInt(s.substring(3));
                } catch (Exception e){
                    e.printStackTrace();
                    seqNum = -1;
                }
            }
        }

        // Attempt to parse message
        if (seqNum == -1){
            // If no sequence number then create failed response
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
                this.writeToClient(key, rejMsg.getMessageString());
            } catch (FixException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (msg.getClass().equals(NewOrderSingleMessage.class)){

            // If message is a new single order, create new order object
            Map<Integer, String> msgTags = ((NewOrderSingleMessage) msg).getTags();
            MarketOrder order = new MarketOrder(this.orderID++, msgTags.get(49), msgTags.get(11), msgTags.get(55), Integer.parseInt(msgTags.get(54)), Integer.parseInt(msgTags.get(38)));

            // Add order to order storage structure
            this.orders.put(order.getId(), order);

            // Send a confirmation message to the client
            try {
                ExecutionReportMessage reportMessage = createExecutionReportMessage(order);
                this.writeToClient(key, reportMessage.getMessageString());
            } catch (FixException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Send the order object to the trader
            try{
                String orderMsg = order.serialize();
                this.writeToServer(orderMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // OM logic methods for trader/OM communication
    /**
     * Creates an execution report for the client
     * @param ord the order object to generate the report on
     * @return
     * @throws FixException
     */
    private ExecutionReportMessage createExecutionReportMessage(Order ord) throws FixException {

        MarketOrder order = (MarketOrder) ord;
        ExecutionReportMessage reportMessage = new ExecutionReportMessage();

        reportMessage.addHeader("Fix.4.4", this.id, order.getClientID(), this.msgCount++);
        reportMessage.addBody(order.getId(), order.getClOrdID(), this.execMsgCount++, '0',
                order.getOrdStatus(), order.getOrdStatus(), order.getSymbol(), order.getSide(), order.getQuantity(),
                order.getQuantity() - order.getCumQuantity(), order.getCumQuantity(), 0);
        reportMessage.addTrailer();
        reportMessage.packageMessage();

        return reportMessage;
    }


    // Misc methods
    /**
     * Returns the key of a hashmap entry with the specified key
     * @param hashMap the hashmap to search
     * @param value the value to use
     * @return
     */
    public static Object getKeyFromValue(Map hashMap, Object value) {
        for (Object o : hashMap.keySet()) {
            if (hashMap.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}