package com.asialocalguide.gateway.viator.exception;

public class ViatorDestinationApiException extends RuntimeException {

  // Constructor with a specific error message
  public ViatorDestinationApiException(String message) {
    super(message);
  }

  // Constructor with a specific cause
  public ViatorDestinationApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
