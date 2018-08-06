package org.m3.js;

import java.util.concurrent.TimeUnit;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {

        Thread thread = new Thread(new BasicOrderManager("localhost", 9093, "OM"));
        thread.start();

        System.out.println("Server started");

        try {
            Client client1 = new Client("FIX.4.4");
            client1.connect("localhost", 9093);
            client1.placeNewMarketOrder("VOD",1, 100);
            client1.placeNewMarketOrder("VOD",1, 100);
            client1.placeNewMarketOrder("VOD",1, 100);
            client1.placeNewMarketOrder("VOD",1, 100);

            client1.disconnect();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
