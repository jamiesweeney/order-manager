package org.m3.js;


import quickfix.fix44.Message;
import quickfix.fix44.NewOrderSingle;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {

        try {


            Thread thread = new Thread(new BasicOrderManager("localhost", 9093));
            thread.start();

            System.out.println("Server started");

            try {
                Client client1 = new Client();
                client1.connect("localhost", 9093);

                Client client2 = new Client();
                client2.connect("localhost", 9093);

                Client client3 = new Client();
                client3.connect("localhost", 9093);

//                client1.placeNewMarketOrder("VOD", 1, 10 );
//                client1.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }





    }
}
