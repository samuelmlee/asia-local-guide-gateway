package com.asialocalguide.gateway.appuser.exception;

/**
 * Thrown when a requested application user cannot be found.
 */
public class AppUserNotFoundException extends RuntimeException {

	/**
	 * @param message the detail message
	 */
	public AppUserNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param message   the detail message
	 * @param throwable the underlying cause
	 */
	public AppUserNotFoundException(String message, Exception throwable) {
		super(message, throwable);
	}
}
