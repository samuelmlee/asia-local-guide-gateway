package com.asialocalguide.gateway.appuser.exception;

public class AppUserNotFoundException extends RuntimeException {

	public AppUserNotFoundException(String message) {
		super(message);
	}

	public AppUserNotFoundException(String message, Exception throwable) {
		super(message, throwable);
	}
}
