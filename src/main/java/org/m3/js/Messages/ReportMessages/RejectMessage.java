package org.m3.js.Messages.ReportMessages;

import org.m3.js.Messages.Message;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class RejectMessage extends Message {

    protected Set<Integer> reqBody = new HashSet<Integer>() {{
        add(45);
        add(58);
    }};

    protected String getMsgType(){
        return "3";
    }

    public void addBody(int refSeqNum, String text){
        body = new LinkedHashMap<>();

        body.put(45, String.valueOf(refSeqNum));
        body.put(58, text);
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
