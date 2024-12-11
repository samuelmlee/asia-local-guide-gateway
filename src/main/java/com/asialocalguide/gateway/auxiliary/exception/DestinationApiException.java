package com.asialocalguide.gateway.auxiliary.exception;

public class DestinationApiException extends RuntimeException {

  public static final String DEFAULT_MESSAGE =
      "Error occurred with request sent to the Destination API";

  // Constructor with a specific error message
  public DestinationApiException(String message) {
    super(message);
  }

  // Constructor with a specific cause
  public DestinationApiException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }
}
