package com.asialocalguide.gateway.core.exception;

public class AuthProviderException extends RuntimeException {

  public AuthProviderException(String message) {
    super(message);
  }

  public AuthProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
