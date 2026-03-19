package com.asialocalguide.gateway.auth.exception;

import lombok.Getter;

/**
 * Thrown when deleting a user from the authentication provider fails.
 *
 * <p>Carries a {@link Type} discriminant so callers can distinguish validation errors
 * from provider-side failures without inspecting the message string.
 */
@Getter
public class ProviderUserDeletionException extends RuntimeException {
	private final Type type;

	/**
	 * @param message the detail message
	 * @param type    the failure category
	 */
	public ProviderUserDeletionException(String message, Type type) {
		super(message);
		this.type = type;
	}

	/**
	 * @param message   the detail message
	 * @param throwable the underlying cause
	 * @param type      the failure category
	 */
	public ProviderUserDeletionException(String message, Exception throwable, Type type) {
		super(message, throwable);
		this.type = type;
	}

	/**
	 * Categorizes the reason a provider user deletion failed.
	 */
	public enum Type {
		VALIDATION, NOT_FOUND, CONSTRAINT_VIOLATION, UNKNOWN
	}
}
