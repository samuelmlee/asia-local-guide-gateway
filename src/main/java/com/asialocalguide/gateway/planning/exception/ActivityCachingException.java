package com.asialocalguide.gateway.planning.exception;

public class ActivityCachingException extends RuntimeException {
	public ActivityCachingException(String message) {
		super(message);
	}

	public ActivityCachingException(String message, Throwable cause) {
		super(message, cause);
	}
}
