package com.asialocalguide.gateway.core.exception;


public class UserCreationException extends RuntimeException {

  public UserCreationException(String s) {
    super(s);
  }

  public UserCreationException(String s, Exception throwable) {
    super(s, throwable);
  }
}
