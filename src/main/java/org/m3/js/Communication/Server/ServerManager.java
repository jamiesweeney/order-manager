package org.m3.js.Communication.Server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;

/**
 * Provides an interface to define the method that the manager of
 * a ServerNode object should provide.
 */
public interface ServerManager {

    // Callback methods
    /**
     * Provides a callback method that should be called on acceptance
     * of a new client
     * @param remoteAddr the address that the client is connecting on
     */
    void acceptClientCallback(SocketAddress remoteAddr);

    /**
     * Provides a callback method that should be called on the
     * disconnection of a client
     * @param remoteAddr the address that the client is disconnecting from
     */
    void cancelClientCallback(SocketAddress remoteAddr);

    /**
     * Provides a callback method that should be called when a
     * new message is read from a client
     * @param key the key that represents the client
     * @param messages the array of messages received
     */
    void readFromClientCallback(SelectionKey key, String[] messages);


    // Node control methods
    /**
     * A method that allows the manager to write to a client
     * through the ServerNode object
     * @param key the key that represents the client
     * @param message the message to write
     * @throws IOException
     */
    void writeToClient(SelectionKey key, String message) throws IOException;
}

