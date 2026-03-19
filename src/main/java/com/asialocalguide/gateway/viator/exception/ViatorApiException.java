package com.asialocalguide.gateway.viator.exception;

/**
 * Thrown when a Viator API call fails with an unexpected HTTP status or a network error.
 */
public class ViatorApiException extends RuntimeException {

	/**
	 * @param message description of the API failure
	 */
	public ViatorApiException(String message) {
		super(message);
	}

	/**
	 * @param message description of the API failure
	 * @param cause   the underlying exception
	 */
	public ViatorApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
