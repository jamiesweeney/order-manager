package org.m3.js;


public class FixException extends Exception{

    public String message;

    public FixException(String message){
        this.message = message;
    }

    // Overrides Exception's getMessage()
    @Override
    public String getMessage(){
        return message;
    }
}