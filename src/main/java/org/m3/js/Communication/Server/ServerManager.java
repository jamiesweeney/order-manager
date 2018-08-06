package org.m3.js.Communication.Server;

import java.nio.channels.SelectionKey;

public interface ServerManager {

    void setServerNode(ServerNode serverNode);

    void readFromClientCallback(SelectionKey key, String message);
}

