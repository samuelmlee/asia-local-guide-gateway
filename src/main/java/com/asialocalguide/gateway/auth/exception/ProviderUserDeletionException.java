package com.asialocalguide.gateway.auth.exception;

import lombok.Getter;

@Getter
public class ProviderUserDeletionException extends RuntimeException {
	private final Type type;

	public ProviderUserDeletionException(String message, Type type) {
		super(message);
		this.type = type;
	}

	public ProviderUserDeletionException(String message, Exception throwable, Type type) {
		super(message, throwable);
		this.type = type;
	}

	public enum Type {
		VALIDATION, NOT_FOUND, CONSTRAINT_VIOLATION, UNKNOWN
	}
}
