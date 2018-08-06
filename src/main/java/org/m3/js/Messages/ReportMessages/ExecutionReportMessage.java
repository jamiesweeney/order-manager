package org.m3.js.Messages.ReportMessages;

import org.m3.js.Messages.Message;
import org.m3.js.Orders.MarketOrder;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class ExecutionReportMessage extends Message {

    protected Set<Integer> reqBody = new HashSet<Integer>() {{
        add(37);
        add(11);
        add(17);
        add(20);
        add(150);
        add(39);
        add(55);
        add(54);
        add(38);
        add(151);
        add(14);
        add(6);
    }};

    protected String getMsgType() {
        return "8";
    }

    public void addBody(long ordID, String clOrdID, int execID, char execTransType, char execType, char ordStatus, String symbol, int side, int orderQty, int leavesQty, int cumQty, float avgPx) {
        body = new LinkedHashMap<>();

        body.put(37, String.valueOf(ordID));
        body.put(11, clOrdID);
        body.put(17, String.valueOf(execID));
        body.put(20, String.valueOf(execTransType));
        body.put(150, String.valueOf(execType));
        body.put(39, String.valueOf(ordStatus));
        body.put(55, symbol);
        body.put(54, String.valueOf(side));
        body.put(38, String.valueOf(orderQty));
        body.put(151, String.valueOf(leavesQty));
        body.put(14, String.valueOf(cumQty));
        body.put(6, String.valueOf(avgPx));
    }

    @Override
    protected boolean isBodyValid(boolean done){

        // Using the required body tags set
        for (Integer key : reqBody){

            // Check each tag exists
            if (!body.containsKey(key)){
                return false;
            }

            if (body.get(key) == null){
                return false;
            }
        }
        return true;
    }


}
