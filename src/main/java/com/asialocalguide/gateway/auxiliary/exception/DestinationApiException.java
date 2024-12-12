package com.asialocalguide.gateway.auxiliary.exception;

public class DestinationApiException extends RuntimeException {

  // Constructor with a specific error message
  public DestinationApiException(String message) {
    super(message);
  }

  // Constructor with a specific cause
  public DestinationApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
