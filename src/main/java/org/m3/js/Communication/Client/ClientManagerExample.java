package org.m3.js.Communication.Client;

import java.io.IOException;
import java.net.SocketAddress;

public class ClientManagerExample implements ClientManager {

    ClientNode clientNode;

    @Override
    public void acceptedByServerCallback(SocketAddress remoteAddr) {
        System.out.println("Client has been accepted by the server on: \"" + remoteAddr.toString() + "\"");
    }

    @Override
    public void disconnectedFromServerCallback(SocketAddress remoteAddr) {
        System.out.println("Client has disconnected from the server on: \"" + remoteAddr.toString() + "\"");
    }

    @Override
    public void readFromServerCallback(String[] messages) {
        for (String message : messages){
            System.out.println("Client has received: \"" + message + "\"");
        }
    }

    @Override
    public void writeToServer(String message) throws IOException {
        this.clientNode.writeToServer(message);
    }
}
