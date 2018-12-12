package org.m3.js.Orders;

import java.io.*;
import java.util.Base64;


public abstract class Order implements Serializable{

    // Reference data
    private final long id;
    private final String clientID;
    private final String clOrdID;
    private char ordStatus;

    // Order data
    private final String symbol;
    private final int side;

    /**
     * Constructs a new order object
     * @param id the id of the order
     * @param clientID the client's id
     * @param clOrdID the client's order id
     * @param symbol the stock symbol
     * @param side buy/sell
     */
    public Order(long id, String clientID, String clOrdID, String symbol, int side){
        this.id = id;
        this.clientID = clientID;
        this.clOrdID = clOrdID;
        this.ordStatus = 'A';

        this.symbol = symbol;
        this.side = side;
    }

    // Getters
    public long getId() {
        return id;
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

    // Standard order functionality
    public void updateOrdStatus(char ordStatus) {
        this.ordStatus = ordStatus;
    }


    // Misc methods
    /**
     * Serializes the order object
     * @return
     * @throws IOException
     */
    public String serialize() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(this);
            final byte[] byteArray = bos.toByteArray();
            return Base64.getEncoder().encodeToString(byteArray);
    }

    /**
     * Deserialises to an order object
     * @param byteString the serial string
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object deserialize(String byteString) throws IOException, ClassNotFoundException{
        final byte[] bytes = Base64.getDecoder().decode(byteString);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis);
        Object obj = in.readObject();
        return obj;
    }

//
//	public int id; //TODO these should all be longs
//	short orderRouter;
//	public int ClientOrderID; //TODO refactor to lowercase C
//	int size;
//	double[]bestPrices;
//	int bestPriceCount;
//
//
//	int clientid;
//	public Instrument instrument;
//	public double initialMarketPrice;
//	ArrayList<Order>slices;
//	ArrayList<Fill>fills;
//	char OrdStatus='A'; //OrdStatus is Fix 39, 'A' is 'Pending New'
//	//Status state;
//
//
//	public Order(int clientId, int ClientOrderID, Instrument instrument, int size){
//		this.ClientOrderID=ClientOrderID;
//		this.size=size;
//		this.clientid=clientId;
//		this.instrument=instrument;
//		fills=new ArrayList<Fill>();
//		slices=new ArrayList<Order>();
//	}
//
//
//
//	public int sliceSizes(){
//		int totalSizeOfSlices=0;
//		for(Order c:slices)totalSizeOfSlices+=c.size;
//		return totalSizeOfSlices;
//	}
//	public int newSlice(int sliceSize){
//		slices.add(new Order(id,ClientOrderID,instrument,sliceSize));
//		return slices.size()-1;
//		return 0;
//	}
//	public int sizeFilled(){
//		int filledSoFar=0;
//		for(Fill f:fills){
//			filledSoFar+=f.size;
//		}
//		for(Order c:slices){
//			filledSoFar+=c.sizeFilled();
//		}
//		return filledSoFar;
//	}
//	public int sizeRemaining(){
//		return size-sizeFilled();
//	}
//
//
//	float price(){
//		//TODO this is buggy as it doesn't take account of slices. Let them fix it
//		float sum=0;
//		for(Fill fill:fills){
//			sum+=fill.price;
//		}
//		return sum/fills.size();
//	}
//	void createFill(int size,double price){
//		fills.add(new Fill(size,price));
//		if(sizeRemaining()==0){
//			OrdStatus='2';
//		}else{
//			OrdStatus='1';
//		}
//	}
//	void cross(Order matchingOrder){
//		//pair slices first and then parent
//		for(Order slice:slices){
//			if(slice.sizeRemaining()==0)continue;
//			//TODO could optimise this to not start at the beginning every time
//			for(Order matchingSlice:matchingOrder.slices){
//				int msze=matchingSlice.sizeRemaining();
//				if(msze==0)continue;
//				int sze=slice.sizeRemaining();
//				if(sze<=msze){
//					slice.createFill(sze,initialMarketPrice);
//					matchingSlice.createFill(sze, initialMarketPrice);
//					break;
//				}
//				//sze>msze
//				slice.createFill(msze,initialMarketPrice);
//				matchingSlice.createFill(msze, initialMarketPrice);
//			}
//			int sze=slice.sizeRemaining();
//			int mParent=matchingOrder.sizeRemaining()-matchingOrder.sliceSizes();
//			if(sze>0 && mParent>0){
//				if(sze>=mParent){
//					slice.createFill(sze,initialMarketPrice);
//					matchingOrder.createFill(sze, initialMarketPrice);
//				}else{
//					slice.createFill(mParent,initialMarketPrice);
//					matchingOrder.createFill(mParent, initialMarketPrice);
//				}
//			}
//			//no point continuing if we didn't fill this slice, as we must already have fully filled the matchingOrder
//			if(slice.sizeRemaining()>0)break;
//		}
//		if(sizeRemaining()>0){
//			for(Order matchingSlice:matchingOrder.slices){
//				int msze=matchingSlice.sizeRemaining();
//				if(msze==0)continue;
//				int sze=sizeRemaining();
//				if(sze<=msze){
//					createFill(sze,initialMarketPrice);
//					matchingSlice.createFill(sze, initialMarketPrice);
//					break;
//				}
//				//sze>msze
//				createFill(msze,initialMarketPrice);
//				matchingSlice.createFill(msze, initialMarketPrice);
//			}
//			int sze=sizeRemaining();
//			int mParent=matchingOrder.sizeRemaining()-matchingOrder.sliceSizes();
//			if(sze>0 && mParent>0){
//				if(sze>=mParent){
//					createFill(sze,initialMarketPrice);
//					matchingOrder.createFill(sze, initialMarketPrice);
//				}else{
//					createFill(mParent,initialMarketPrice);
//					matchingOrder.createFill(mParent, initialMarketPrice);
//				}
//			}
//		}
//	}
//	void cancel(){
//		//state=cancelled
//	}



}


