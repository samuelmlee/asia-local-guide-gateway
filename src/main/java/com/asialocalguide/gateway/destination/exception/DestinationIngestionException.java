package com.asialocalguide.gateway.destination.exception;

/**
 * Thrown when destination data cannot be ingested from an external provider.
 */
public class DestinationIngestionException extends RuntimeException {

	/**
	 * @param message the detail message
	 */
	public DestinationIngestionException(String message) {
		super(message);
	}

	/**
	 * @param message   the detail message
	 * @param throwable the underlying cause
	 */
	public DestinationIngestionException(String message, Exception throwable) {
		super(message, throwable);
	}
}
