package org.m3.js.Communication.Client;

import java.io.IOException;

public class ClientManagerImpl implements ClientManager {

    ClientNode clientNode;

    public void setClientNode(ClientNode clientNode) {
        this.clientNode = clientNode;
    }

    public void readFromServerCallback(String message) {
        System.out.println("ClientManager: " + message);
        try {
            this.clientNode.writeToServer("Another message");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
