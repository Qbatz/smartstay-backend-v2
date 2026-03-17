package com.smartstay.smartstay.Exceptions;

public class SmartStayException extends NullPointerException {
    private final String message;

    public SmartStayException() {
       message = "Not found";
    }

    public SmartStayException(String message) {
        super(message);
        this.message = message;
    }
}
