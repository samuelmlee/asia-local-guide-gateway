package com.asialocalguide.gateway.auth.exception;

/**
 * Thrown when communication with or an operation on the external authentication provider fails.
 */
public class AuthProviderException extends RuntimeException {

	/**
	 * @param message the detail message
	 */
	public AuthProviderException(String message) {
		super(message);
	}

	/**
	 * @param message the detail message
	 * @param cause   the underlying cause
	 */
	public AuthProviderException(String message, Throwable cause) {
		super(message, cause);
	}
}
