package com.asialocalguide.gateway.viator.exception;

public class ViatorApiException extends RuntimeException {

  // Constructor with a specific error message
  public ViatorApiException(String message) {
    super(message);
  }

  // Constructor with a specific cause
  public ViatorApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
