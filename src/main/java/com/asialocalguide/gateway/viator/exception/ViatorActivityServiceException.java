package com.asialocalguide.gateway.viator.exception;

/**
 * Thrown when fetching or processing Viator activity data fails in the service layer.
 */
public class ViatorActivityServiceException extends RuntimeException {

	/**
	 * @param message description of the service failure
	 */
	public ViatorActivityServiceException(String message) {
		super(message);
	}

	/**
	 * @param message description of the service failure
	 * @param cause   the underlying exception
	 */
	public ViatorActivityServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
