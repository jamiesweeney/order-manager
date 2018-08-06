package org.m3.js.Messages;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class Message {

    protected Map<Integer, String> header;
    protected Map<Integer, String> body;
    protected Map<Integer, String> trailer;

    protected Set<Integer> reqHeader = new HashSet<Integer>() {{
        add(8);
        add(9);
        add(35);
        add(49);
        add(56);
        add(34);
        add(52);
    }};

    protected Set<Integer> reqBody;

    protected Set<Integer> reqTrailer = new HashSet<Integer>() {{
        add(10);
    }};

    private String messageStr;


    /**
     * Returns the message type code, is overridden for specific
     * message types
     *
     * @return msgType
     */
    protected String getMsgType(){
        return null;
    }

    // Public methods for class usage
    public void addHeader(String version, String sCompID, String tCompID, int msgSeqNum) throws FixException {
        header = new LinkedHashMap<>();

        if (version == null){
            throw new FixException("Fix version tag cannot be null");
        }
        if (sCompID == null){
            throw new FixException("Fix sCompID tag cannot be null");
        }
        if (tCompID == null){
            throw new FixException("Fix tCompID tag cannot be null");
        }
        if(Integer.valueOf(msgSeqNum) == null){
            throw new FixException("Fix msgSeqNum tag cannot be null");
        }

        header.put(8, version);
        header.put(9, null);
        header.put(35,getMsgType());
        header.put(49, sCompID);
        header.put(56, tCompID);
        header.put(34, String.valueOf(msgSeqNum));
        header.put(52, null);
    }

    public void addTrailer(){
        trailer = new LinkedHashMap<>();

        trailer.put(10, null);
    }

    public void packageMessage() throws FixException {

        // Pre-package check
        if (!isValid(false)){
            throw new FixException("Message cannot be packaged, initial conditions invalid.");
        }

        if (messageStr != null){
            throw new FixException("Message cannot be packaged, message has already been packaged.");
        }

        // Add missing tags= values
        addUTCTimestamp();
        addMessageLength();
        addChecksum();

        // Post-package check
        if (!isValid(true)){
            throw new FixException("Message cannot be packaged, invalid packaging done.");
        }

        createMessageString();
    }

    public String getMessageString(){
        return this.messageStr;
    }

    public static final Message parseFromText(String message) {

        // Try and parse the message
        try{

            // Parse to tags
            Map<Integer,String> tags = new LinkedHashMap<>();
            String[] tagArr = message.split("\\|");
            for (String tag : tagArr){
                String[] elems = tag.split("=");

                try{
                    tags.put(Integer.parseInt(elems[0]), elems[1]);
                }catch (NumberFormatException e){
                    return new FailedMessage("FIX-EX: Could not parse \"" + tag + "\"");
                }
            }


            if (!tags.containsKey(35)){
                return new FailedMessage("FIX-EX: Missing tag 35");
            }

            switch (tags.get(35)){
                case "0": break;
                case "1": break;
                case "2": break;
                case "3": break;
                case "4": break;
                case "5": break;
                case "6": break;
                case "7": break;
                case "8": break;
                case "9": break;
                case "A": break;
                case "B": break;
                case "C": break;

                // New Order Single
                case "D": NewOrderSingleMessage msgObj = new NewOrderSingleMessage();
                    msgObj.parse(message);
                    return msgObj;
                case "E": break;
                case "F": break;
                case "G": break;
                case "H": break;
                case "I": break;
                case "J": break;
                case "K": break;
                case "L": break;
                case "M": break;
                case "N": break;
                case "O": break;
                case "P": break;
                case "Q": break;
                case "R": break;
                case "S": break;
                case "T": break;
                case "U": break;
                case "V": break;
                case "W": break;
                case "X": break;
                case "Y": break;
                case "Z": break;
                case "a": break;
                case "b": break;
                case "c": break;
                case "d": break;
                case "e": break;
                case "f": break;
                case "g": break;
                case "h": break;
                case "i": break;
                case "j": break;
                case "k": break;
                case "l": break;
                case "m": break;
                default:
                    return new FailedMessage("FIX-EX: Unknown message type \"" + tags.get(35) + "\"");
            }
            return new FailedMessage("FIX-EX: Unsupported message type \"" + tags.get(35) + "\"");
        }catch (FixException e){
            return new FailedMessage(e.getMessage());
        }
    }


    // Methods for getting a message string from the message
    protected void createMessageString(){

        // Combine all tags
        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(header.entrySet());
            addAll(body.entrySet());
            addAll(trailer.entrySet());
        }};

        StringBuilder sb =  new StringBuilder();

        for (Map.Entry<Integer,String> entry : entries){

            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("|");
        }
        String message = sb.substring(0, sb.length()-1);

        messageStr=message;
    }


    // Methods for checking the validity of the message
    // done - true if the message has been packaged yet
    protected boolean isValid(boolean done){

        if (header == null || body == null || trailer == null){
            return false;
        }

        return isHeaderValid(done) && isBodyValid(done) && isTrailerValid(done);
    }

    protected boolean isHeaderValid(boolean done){

        // Using the required header tags set
        for (Integer key : reqHeader){

            // Check each tag exists
            if (!header.containsKey(key)){
                return false;
            }

            // Remember length and time should be null if done == false
            if (!(key == 9 || key == 52) || done){
                if (header.get(key) == null){
                    return false;
                }
            }
        }
        return true;
    }

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

    protected boolean isTrailerValid(boolean done){

        // Using the required trailer tags set
        for (Integer key : reqTrailer){

            // Check each tag exists
            if (!trailer.containsKey(key)){
                return false;
            }

            // Remember checksum should be null just now
            if (key != 10){
                if (done && trailer.get(key) == null){
                    return false;
                }
            }
        }
        return true;
    }


    // Methods for supporting message length tag
    protected void addMessageLength(){
        int length = countMessageLength();
        header.put(9, String.valueOf(length));
    }

    protected int countMessageLength(){

        // Create a list of tags up to the checksum
        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(header.entrySet());
            addAll(body.entrySet());
            addAll(trailer.entrySet());
        }};

        return countFromTags(entries);
    }

    protected int countFromTags(List<Map.Entry<Integer,String>> entries){
        int length = 0;

        // Iterate over the tags
        for (Map.Entry<Integer,String> entry : entries) {

            // Skip first 2 tags and stop at the checksum
            if (entry.getKey().equals(8) || entry.getKey().equals(9)) {
                continue;
            } else if (entry.getKey().equals(10)) {
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


    // Methods for supporting the checksum tag
    protected void addChecksum(){
        String checksum = String.valueOf(countChecksum());

        // Pad with zeroes
        while (checksum.length() < 3){
            checksum = "0" + checksum;
        }

        // Insert into fix message
        trailer.put(10, String.valueOf(checksum));
    }

    protected int countChecksum(){

        List<Map.Entry<Integer,String>> entries = new LinkedList<Map.Entry<Integer,String>>(){{
            addAll(header.entrySet());
            addAll(body.entrySet());
            addAll(trailer.entrySet());
        }};

        return checkFromTags(entries);
    }

    protected int checkFromTags(List<Map.Entry<Integer,String>> entries){
        int total = 0;

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

    protected int sumASCII(String str) {
        int sum = 0;
        for (char c : str.toCharArray()) {
            sum += (int) c;
        }
        return sum;
    }


    // Methods for supporting the sending time tag
    protected void addUTCTimestamp(){
        String timestamp = getUTCTimestamp();
        header.put(52, timestamp);
    }

    protected String getUTCTimestamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
