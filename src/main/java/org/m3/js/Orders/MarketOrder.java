package org.m3.js.Orders;

import java.io.Serializable;

public class MarketOrder extends Order implements Serializable {

    // Order data
    private final int quantity;
    private int cumQty;


    public MarketOrder(long id, String clientID, String clOrdID, String symbol, int side, int quantity){
        super(id, clientID, clOrdID, symbol, side);
        this.quantity = quantity;
        this.cumQty = 0;
    }

    // Getters
    public int getQuantity() {
        return quantity;
    }
    public int getCumQuantity() {
        return cumQty;
    }

    // Functionality
    public void fill(int amount){
        this.cumQty += amount;
    }
    public void sliceOrder(int size){

    }
}
