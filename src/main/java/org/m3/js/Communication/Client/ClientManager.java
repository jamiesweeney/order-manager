package org.m3.js.Communication.Client;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Provides an interface to define the method that the manager of
 * a ClientNode object should provide.
 */
public interface ClientManager {

    // Callback methods
    /**
     * Provides a callback method that should be called on acceptance
     * by the server
     * @param remoteAddr the address that of the server
     */
    void acceptedByServerCallback(SocketAddress remoteAddr);

    /**
     * Provides a callback method that should be called when the
     * server disconnects
     * @param remoteAddr the address that the server is disconnecting from
     */
    void disconnectedFromServerCallback(SocketAddress remoteAddr);

    /**
     * Provides a callback method that should be called when a
     * new message is read from the server
     * @param messages the array of messages received
     */
    void readFromServerCallback(String[] messages);


    // Node control methods
    /**
     * A method that allows the manager to write to the server
     * through the clientNode object
     * @param message the message to write
     * @throws IOException
     */
    void writeToServer(String message) throws IOException;

}
