package com.asialocalguide.gateway.core.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Exception throwable) {
    super(message, throwable);
  }
}
