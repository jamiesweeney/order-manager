package org.m3.js;

import org.m3.js.Communication.Client.ClientManager;
import org.m3.js.Communication.Client.ClientManagerImpl;
import org.m3.js.Communication.Client.ClientNode;
import org.m3.js.Communication.Server.ServerManagerImpl;
import org.m3.js.Communication.Server.ServerNode;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) {

        ServerManagerImpl smi = new ServerManagerImpl();
        ClientManagerImpl cmi = new ClientManagerImpl();

        ServerNode sn = new ServerNode("localhost", 1000, smi);
        ClientNode cn = new ClientNode(cmi);

        smi.setServerNode(sn);
        cmi.setClientNode(cn);

        Thread st = new Thread(sn);
        st.start();

        try {
            cn.connect("localhost", 1000);
            cn.writeToServer("IS THIS WORKING");
            cn.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        try{
//            Thread t = new Thread(new Trader("localhost", 1000));
//            t.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        Thread thread = new Thread(new BasicOrderManager("localhost", 9093, "OM"));
//        thread.start();

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
