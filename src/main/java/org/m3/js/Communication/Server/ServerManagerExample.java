package org.m3.js.Communication.Server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;

public class ServerManagerExample implements ServerManager {

    ServerNode serverNode;

    public ServerManagerExample(String address, int port){
        this.serverNode = new ServerNode(address, port, this);
    }

    @Override
    public void acceptClientCallback(SocketAddress remoteAddr) {
        System.out.println("New client has been accepted on: \"" + remoteAddr.toString() + "\"");
    }

    @Override
    public void cancelClientCallback(SocketAddress remoteAddr) {
        System.out.println("Client has been disconnected from: \"" + remoteAddr.toString() + "\"");
    }

    @Override
    public void readFromClientCallback(SelectionKey key, String[] messages) {
        for (String message : messages){
            System.out.println("Server has received: \"" + message + "\"");
        }
    }

    @Override
    public void writeToClient(SelectionKey key, String message) throws IOException {
        this.serverNode.writeToClient(key, message);
    }
}
