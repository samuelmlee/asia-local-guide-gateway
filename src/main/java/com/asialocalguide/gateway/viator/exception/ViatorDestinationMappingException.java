package com.asialocalguide.gateway.viator.exception;

public class ViatorDestinationMappingException extends RuntimeException {
  public ViatorDestinationMappingException(String message) {
    super(message);
  }

  public ViatorDestinationMappingException(String message, Throwable cause) {
    super(message, cause);
  }
}
