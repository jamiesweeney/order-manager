package org.m3.js.Messages;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class Message {

    private Map<Integer, String> header;
    private Map<Integer, String> body;
    private Map<Integer, String> trailer;

    String msgType = null;

    private Set<Integer> reqHeader = new HashSet<Integer>() {{
        add(8);
        add(9);
        add(35);
        add(49);
        add(56);
        add(34);
        add(52);
    }};

    private Set<Integer> reqBody;

    private Set<Integer> reqTrailer = new HashSet<Integer>() {{
        add(10);
    }};


    public void addHeader(String version, String sCompID, String tCompID, String msgSeqNum){
        header = new LinkedHashMap<>();

        header.put(8, version);
        header.put(9, null);
        header.put(35,this.msgType);
        header.put(49, sCompID);
        header.put(56, tCompID);
        header.put(34, msgSeqNum);
        header.put(52, null);
    }

    public void addTrailer(){

        trailer.put(10, null);
    }

}
