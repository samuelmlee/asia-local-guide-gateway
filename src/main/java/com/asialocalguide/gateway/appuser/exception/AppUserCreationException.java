package com.asialocalguide.gateway.appuser.exception;

/**
 * Thrown when application user creation fails, for example due to a duplicate email
 * or an error while persisting the user.
 */
public class AppUserCreationException extends RuntimeException {

	/**
	 * @param s the detail message
	 */
	public AppUserCreationException(String s) {
		super(s);
	}

	/**
	 * @param s         the detail message
	 * @param throwable the underlying cause
	 */
	public AppUserCreationException(String s, Exception throwable) {
		super(s, throwable);
	}
}
