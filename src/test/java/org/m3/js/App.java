package org.m3.js;

import org.m3.js.OrderManager.BasicOrderManager;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {

        // Start trader thread
        RandomTrader randomTrader = new RandomTrader("localhost", 9093);
        Thread traderThread = new Thread(randomTrader);
        traderThread.start();

        // Start OM thread
        Thread omThread = new Thread(new BasicOrderManager("localhost", 9092,"localhost", 9093, "OM"));
        omThread.start();

        // Start Client threads
        new Thread(new BetterClient("localhost", 9092)).start();
    }
}
