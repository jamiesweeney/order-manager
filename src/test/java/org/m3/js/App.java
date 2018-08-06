package org.m3.js;

import org.m3.js.OrderManager.BasicOrderManager;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {

        try{
            Thread t = new Thread(new Trader("localhost", 1000));
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Thread thread = new Thread(new BasicOrderManager("localhost", 9093, "OM"));
        thread.start();

//        try {
//            Client client1 = new Client("FIX.4.4");
//            client1.connect("localhost", 9093);
//            client1.placeNewMarketOrder("VOD",1, 100);
//            client1.listen();
//
//            //            client1.disconnect();
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
