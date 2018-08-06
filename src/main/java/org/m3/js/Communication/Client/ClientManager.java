package org.m3.js.Communication.Client;

public interface ClientManager {

    void setClientNode(ClientNode clientNode);

    void readFromServerCallback(String message);

}
