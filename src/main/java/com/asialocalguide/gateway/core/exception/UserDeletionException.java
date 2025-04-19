package com.asialocalguide.gateway.core.exception;

import lombok.Getter;

@Getter
public class UserDeletionException extends RuntimeException {
  private final Type type;

  public UserDeletionException(String message, Type type) {
    super(message);
    this.type = type;
  }

  public UserDeletionException(String message, Exception throwable, Type type) {
    super(message, throwable);
    this.type = type;
  }

  public enum Type {
    VALIDATION,
    NOT_FOUND,
    CONSTRAINT_VIOLATION,
    UNKNOWN
  }
}
