package com.asialocalguide.gateway.viator.exception;

/**
 * Thrown when mapping a Viator destination response to a {@link com.asialocalguide.gateway.destination.domain.CommonDestination} fails.
 */
public class ViatorDestinationMappingException extends RuntimeException {

	/**
	 * @param message description of the mapping failure
	 */
	public ViatorDestinationMappingException(String message) {
		super(message);
	}

	/**
	 * @param message description of the mapping failure
	 * @param cause   the underlying exception
	 */
	public ViatorDestinationMappingException(String message, Throwable cause) {
		super(message, cause);
	}
}
