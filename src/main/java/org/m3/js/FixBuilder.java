package org.m3.js;

import java.text.SimpleDateFormat;
import java.util.*;

public class FixBuilder {


    String account;

    Map<Integer, String> hTags;
    Map<Integer, String>  bTags;
    Map<Integer, String>  fTags;


    // List of supported versions
    List<String> supportedVersions = new ArrayList<String>(){{
        add("FIX.4.4");
    }};

    // List of supported messages
    List<String> supportedMessages = new ArrayList<String>(){{
        add("D");
    }};

    String msgType;
    static List<String> required = new ArrayList();


    public FixBuilder() throws FixException {
    }

    public void addHeader(String ver, String msgType) throws FixException {
        hTags = new LinkedHashMap();

        // Add version if supported, or throw exception
        if (supportedVersions.contains(ver)){
            hTags.put(8, ver);
        }else{
            throw new FixException("Unsupported FIX version given");
        }

        // Add blank message length header
        hTags.put(9, null);

        // Add the message type
        if (supportedMessages.contains(msgType)){
            hTags.put(35, msgType);
        }else{
            throw new FixException("Unsupported message type given");
        }
    }

    private void addMessageLength(){
        int length = getMessageLength();
        hTags.put(9, String.valueOf(length));
    }

    public void addBody(String account, int clOrdId, String symbol, int side, int quantity) throws FixException {
        bTags = new LinkedHashMap();

        // Must have already provided a header
        if (this.hTags == null){
            throw new FixException("No FIX header");
        }

        this.bTags.put(1, account);
        this.bTags.put(11, String.valueOf(clOrdId));
        this.bTags.put(55, symbol);        // Symbol
        this.bTags.put(54, String.valueOf(side));          // Side
        this.bTags.put(38, String.valueOf(quantity));      // Order Quantity
        this.bTags.put(40, "1");           // Order Type (market)
        this.bTags.put(60, getUTCTimestamp());          // Transaction Time
        this.bTags.put(21, "1");             // HandlInst
    }

    public void addFooter() throws FixException {
        fTags = new LinkedHashMap();

        // Must have already provided a header
        if (this.hTags == null){
            throw new FixException("No FIX header");
        }

        // Must have already provided a body
        if (this.bTags == null){
            throw new FixException("No FIX body");
        }

        // Add blank checksum tag
        fTags.put(10, null);
    }

    private void addChecksum(){
        String checksum = String.valueOf(getChecksum());

        // Pad with zeroes
        while (checksum.length() < 3){
            checksum = "0" + checksum;
        }

        // Insert into fix message
        fTags.put(10, String.valueOf(checksum));
    }


    public String getMessageString() throws FixException {
        // Must have already provided a header
        if (this.hTags == null){
            throw new FixException("No FIX header");
        }

        // Must have already provided a body
        if (this.bTags == null){
            throw new FixException("No FIX body");
        }

        // Must have already provided a footer
        if (this.bTags == null){
            throw new FixException("No FIX footer");
        }

        // Fill in blanks (length and checksum)
        addMessageLength();
        addChecksum();

        // Combine all tags
        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(hTags.entrySet());
            addAll(bTags.entrySet());
            addAll(fTags.entrySet());
        }};

        StringBuilder sb =  new StringBuilder();

        for (Map.Entry<Integer,String> entry : entries){

            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("|");
        }
        String message = sb.substring(0, sb.length()-1);
        return message;
    }


    private String getUTCTimestamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private int getMessageLength(){
        int length = 0;

        // Create a list of tags up to the checksum
        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(hTags.entrySet());
            addAll(bTags.entrySet());
            addAll(fTags.entrySet());

        }};

        // Iterate over the tags
        for (Map.Entry<Integer,String> entry : entries){

            // Skip first 2 tags and stop at the checksum
            if (entry.getKey().equals(8) || entry.getKey().equals(9)){
                continue;
            }else if (entry.getKey().equals(10)){
                break;
            }

            // Add key=value|
            length += entry.getKey().toString().length();
            length += 1;
            length += entry.getValue().length();
            length += 1;
        }

        return length;
    }

    private int getChecksum(){
        int total = 0;

        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(hTags.entrySet());
            addAll(bTags.entrySet());
            addAll(fTags.entrySet());
        }};

        // Iterate over the tags
        for (Map.Entry<Integer,String> entry : entries){

            if (entry.getKey().equals(10)){
                break;
            }

            total += sumASCII(entry.getKey().toString());
            total += 61;        //ascii val for =
            total += sumASCII(entry.getValue());
            total += 124;       //ascii val for |
        }

        return total % 256;
    }

    private int sumASCII(String str) {
        int sum = 0;
        for (char c : str.toCharArray()) {
            sum += (int) c;
        }
        return sum;
    }
}
