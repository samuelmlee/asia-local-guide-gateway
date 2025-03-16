package com.asialocalguide.gateway.viator.exception;

public class ViatorActivityAvailabilityMappingException extends RuntimeException{

    // Constructor with a specific error message
    public ViatorActivityAvailabilityMappingException(String message) {
        super(message);
    }

    // Constructor with a specific cause
    public ViatorActivityAvailabilityMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
