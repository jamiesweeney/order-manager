package org.m3.js;

import org.m3.js.Communication.Client.ClientManager;
import org.m3.js.Communication.Client.ClientNode;
import org.m3.js.Messages.FixException;
import org.m3.js.Messages.NewOrderSingleMessage;

import java.io.IOException;
import java.net.SocketAddress;

public class BetterClient implements Runnable, ClientManager {

    ClientNode clientNode;

    private String fixVersion = "FIX.4.4";
    private int msgCount = 0;

    private String clientID;
    private String OMID;


    public BetterClient(String hostname, int port){
        this.clientNode = new ClientNode(hostname, port, this);
        try {
            clientNode.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread listenThread = new Thread(clientNode);
        listenThread.start();
    }

    public void placeNewMarketOrder(String symbol, int side, int quantity){
        try {

            int newID = this.msgCount++;
            String clOrdID = this.clientID + "_" + newID;
            NewOrderSingleMessage nosm = new NewOrderSingleMessage();
            nosm.addHeader(this.fixVersion, this.clientID, this.OMID, newID);
            nosm.addBody(clOrdID, symbol, side, quantity, "1");
            nosm.addTrailer();
            nosm.packageMessage();
            String message = nosm.getMessageString();
            this.writeToServer(message);

            // Only want to add one if successful
        } catch (FixException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void acceptedByServerCallback(SocketAddress remoteAddr) {
        try {
            this.clientNode.writeToServer("ID_REQUEST");
            String resp = this.clientNode.readAllFromServer()[0];
            String[] ids = resp.split("\\|");


            // Set ids
            this.clientID = ids[0];
            this.OMID = ids[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnectedFromServerCallback(SocketAddress remoteAddr) {
    }

    @Override
    public void readFromServerCallback(String[] messages) {
        for (String message : messages){
            System.out.println("Client got: " + message);
        }
    }

    @Override
    public void writeToServer(String message) throws IOException {
        this.clientNode.writeToServer(message);
    }

    @Override
    public void run() {
        this.placeNewMarketOrder("VOD", 1, 100);
//        this.placeNewMarketOrder("VOD", 1, 100);
//        this.placeNewMarketOrder("VOD", 1, 100);
//        this.placeNewMarketOrder("VOD", 1, 100);
    }
}
