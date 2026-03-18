package com.asialocalguide.gateway.appuser.exception;

public class AppUserCreationException extends RuntimeException {

	public AppUserCreationException(String s) {
		super(s);
	}

	public AppUserCreationException(String s, Exception throwable) {
		super(s, throwable);
	}
}
