package org.m3.js;

import org.m3.js.Communication.Server.ServerManager;
import org.m3.js.Communication.Server.ServerNode;
import org.m3.js.Orders.Order;
import org.m3.js.Orders.MarketOrder;
import org.m3.js.TradeScreen.TradeScreen;

import java.io.*;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ThreadLocalRandom;


/**
 *
 * This is a simple NIO based server.
 *
 */
public class RandomTrader implements Runnable, ServerManager, TradeScreen {


    public ServerNode serverNode;
    private Thread serverListenThread;

    public RandomTrader(String address, int port){

        this.serverNode = new ServerNode(address, port, this);
        this.serverListenThread = new Thread(serverNode);
    }

    @Override
    public void run() {
        this.start();
    }

    public void start(){
        this.serverListenThread.start();
    }

    @Override
    public void acceptClientCallback(SocketAddress remoteAddr) {
        System.out.println("IN TRADER Accept");
    }

    @Override
    public void cancelClientCallback(SocketAddress remoteAddr) {
        System.out.println("IN TRADER cancel");
    }

    @Override
    public void readFromClientCallback(SelectionKey key, String[] messages) {

        for (String message : messages){
            System.out.println("RandomTrader got: " + message);
            try {
                Object o = Order.deserialize(message);

                String className = o.getClass().getCanonicalName();
                switch (className) {

                    case "org.m3.js.Orders.MarketOrder":
                        System.out.println("this is a market order");
                        MarketOrder order = (MarketOrder) o;
                        newOrder(order);
                        try {
                            String orderMsg = order.serialize();
                            writeToClient(key, orderMsg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("I dont know what this is");
                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void writeToClient(SelectionKey key, String message) throws IOException {
        this.serverNode.writeToClient(key, message);
    }

    // TradeScreen methods
    @Override
    public void newOrder(Order order) {

        if (order.getOrdStatus() == 'A'){
            int randomNum = ThreadLocalRandom.current().nextInt(0, 1 + 1);

            // Randomly choose between accept,decline,slice
            switch (randomNum){
                case 0:
                    acceptOrder(order);
                    break;

                case 1:
                    declineOrder(order);
                    break;

                case 2:
                    sliceOrder(order);
                    break;

                default:
                    System.out.println("What happened?");
            }
        }
    }
    @Override
    public void acceptOrder(Order order) {
        System.out.println("Accepting order");
        order.updateOrdStatus('0');
    }
    @Override
    public void declineOrder(Order order) {
        System.out.println("Declining order");
        order.updateOrdStatus('8');
    }
    @Override
    public void sliceOrder(Order order) {
        System.out.println("Slicing order");
        // Something here
    }
    @Override
    public void price(Order order) {
        // Something here
    }
}