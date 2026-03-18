package com.asialocalguide.gateway.destination.exception;

public class DestinationIngestionException extends RuntimeException {

	public DestinationIngestionException(String message) {
		super(message);
	}

	public DestinationIngestionException(String message, Exception throwable) {
		super(message, throwable);
	}
}
