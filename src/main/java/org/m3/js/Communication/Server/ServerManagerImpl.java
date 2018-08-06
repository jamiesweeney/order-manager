package org.m3.js.Communication.Server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class ServerManagerImpl implements ServerManager {

    ServerNode serverNode;

    public void setServerNode(ServerNode serverNode){
        this.serverNode = serverNode;
    }

    public void readFromClientCallback(SelectionKey key, String message) {
        System.out.println(message);
        try {
            this.serverNode.writeToClient(key, "Another message");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
