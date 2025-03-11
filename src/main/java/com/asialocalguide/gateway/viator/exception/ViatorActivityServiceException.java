package com.asialocalguide.gateway.viator.exception;

public class ViatorActivityServiceException  extends RuntimeException{

    // Constructor with a specific error message
    public ViatorActivityServiceException(String message) {
        super(message);
    }

    // Constructor with a specific cause
    public ViatorActivityServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
