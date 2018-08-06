package org.m3.js.Orders;

public class MarketOrder {

    // Instance vars
    // Ref data
    private final long ID;
    private final String clientID;
    private final String clOrdID;
    private char ordStatus;


    // Order data
    private final String symbol;
    private final int side;
    private final int quantity;
    private int cumQty;


    public MarketOrder(long id, String clientID, String clOrdID, String symbol, int side, int quantity){
        this.ID = id;
        this.clientID = clientID;
        this.clOrdID = clOrdID;
        this.ordStatus = 'A';
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.cumQty = 0;
    }

    // Getters
    public long getID() {
        return ID;
    }
    public String getClientID() {
        return clientID;
    }
    public String getClOrdID() {
        return clOrdID;
    }
    public char getOrdStatus() {
        return ordStatus;
    }
    public String getSymbol() {
        return symbol;
    }
    public int getSide() {
        return side;
    }
    public int getQuantity() {
        return quantity;
    }
    public int getCumQuantity() {
        return cumQty;
    }

    // Functionality
    public void updateOrdStatus(char ordStatus) {
        this.ordStatus = ordStatus;
    }
    public void fill(int amount){
        this.cumQty += amount;
    }


}
