package com.asialocalguide.gateway.viator.exception;

/**
 * Thrown when mapping Viator availability data to an {@link com.asialocalguide.gateway.planning.domain.ActivityPlanningData} fails.
 */
public class ViatorActivityAvailabilityMappingException extends RuntimeException {

	/**
	 * @param message description of the mapping failure
	 */
	public ViatorActivityAvailabilityMappingException(String message) {
		super(message);
	}

	/**
	 * @param message description of the mapping failure
	 * @param cause   the underlying exception
	 */
	public ViatorActivityAvailabilityMappingException(String message, Throwable cause) {
		super(message, cause);
	}
}
