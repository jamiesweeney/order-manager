package org.m3.js.Messages;

import java.util.*;

public class NewOrderSingleMessage extends Message{


    protected String getMsgType(){
        return "D";
    }

    private Set<Integer> reqBody = new HashSet<Integer>() {{
        add(11);
        add(55);
        add(54);
        add(60);
        add(38);
        add(40);
    }};


    public void addBody(String clOrdID, String symbol, int side, int quantity, String ordType){
        body = new LinkedHashMap<>();

        body.put(11, clOrdID);
        body.put(55, symbol);
        body.put(54, String.valueOf(side));
        body.put(60, null);
        body.put(38, String.valueOf(quantity));
        body.put(40, ordType);
    }


    public void parse(String message) throws FixException {

        // Split the message into tags
        Map<Integer,String> tags = new LinkedHashMap<>();
        String[] tagArr = message.split("\\|");
        for (String tag : tagArr){
            String[] elems = tag.split("=");
            tags.put(Integer.parseInt(elems[0]), elems[1]);
        }

        // Combine all required tags
        Set<Integer> reqAll = new LinkedHashSet<Integer>(){{
            addAll(reqHeader);
            addAll(reqBody);
            addAll(reqTrailer);
        }};

        // Check for all required tags
        for (Integer tag : reqAll){
            if (!tags.containsKey(tag)){
                throw new FixException("FIX-EX: Missing tag " + tag.toString());
            }
            if (tags.get(tag).isEmpty() || tags.get(tag) == null){
                throw new FixException("FIX-EX: Tag " + tag.toString() + " has no value");
            }
        }


        List entryList = new LinkedList<Map.Entry<Integer, String>>();
        entryList.addAll(tags.entrySet());

        // Check length is correct
        int length = this.countFromTags(entryList);
        if (length != Integer.parseInt(tags.get(9))){
            throw new FixException("FIX-EX: Length is not correct, have \"" + tags.get(9) + "\" expected \"" + length + "\"");
        }

        // Check checksum matches
        int checksum = this.checkFromTags(entryList);
        if (checksum != Integer.parseInt(tags.get(10))){
            throw new FixException("FIX-EX: Checksum is not correct, have \"" + tags.get(10) + "\" expected \"" + checksum + "\"");
        }

    }

    @Override
    protected boolean isBodyValid(boolean done){

        // Using the required body tags set
        for (Integer key : reqBody){

            // Check each tag exists
            if (!body.containsKey(key)){
                return false;
            }

            if ((key != 60) || done) {
                if (body.get(key) == null) {
                    return false;
                }
            }
        }
        return true;
    }


    // Methods for supporting the sending time tag
    @Override
    protected void addUTCTimestamp(){
        String timestamp = getUTCTimestamp();
        header.put(52, timestamp);
        body.put(60, timestamp);
    }
}
