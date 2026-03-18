package com.asialocalguide.gateway.planning.exception;

/**
 * Thrown when fetching or caching activities from a booking provider fails.
 */
public class ActivityCachingException extends RuntimeException {

	/**
	 * @param message description of the caching failure
	 */
	public ActivityCachingException(String message) {
		super(message);
	}

	/**
	 * @param message description of the caching failure
	 * @param cause   the underlying exception
	 */
	public ActivityCachingException(String message, Throwable cause) {
		super(message, cause);
	}
}
