package com.asialocalguide.gateway.core.exception;

public class DestinationRepositoryException extends RuntimeException {

  public DestinationRepositoryException(String message) {
    super(message);
  }

  public DestinationRepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
