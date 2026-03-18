package com.asialocalguide.gateway.core.exception;

import com.asialocalguide.gateway.appuser.exception.AppUserCreationException;
import com.asialocalguide.gateway.appuser.exception.AppUserNotFoundException;
import com.asialocalguide.gateway.auth.exception.AuthProviderException;
import com.asialocalguide.gateway.auth.exception.ProviderUserDeletionException;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(AppUserNotFoundException.class)
	public ProblemDetail handleUserNotFoundException(AppUserNotFoundException e) {
		log.error("Failed to find user: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(AppUserCreationException.class)
	public ProblemDetail handleUserCreationException(AppUserCreationException e) {
		log.error("Failed to create user: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(ProviderUserDeletionException.class)
	public ProblemDetail handleUserDeletionException(ProviderUserDeletionException e) {

		if (ProviderUserDeletionException.Type.VALIDATION.equals(e.getType())) {
			log.error("Invalid request parameters: {}", e.getMessage(), e);
			return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		} else {
			log.error("User deletion operation failed: {}", e.getMessage(), e);
			return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@ExceptionHandler(AuthProviderException.class)
	public ProblemDetail handleAuthProviderException(AuthProviderException e) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		Throwable cause = e.getCause();

		if (cause instanceof FirebaseAuthException firebaseEx
				&& firebaseEx.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
			status = HttpStatus.NOT_FOUND;
			log.error("Auth provider error - User not found: {}", e.getMessage(), e);
		} else {
			log.error("Auth provider error: {}", e.getMessage(), e);
		}

		return ProblemDetail.forStatusAndDetail(status, e.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ProblemDetail handleRuntimeException(RuntimeException e) {
		log.error("Unexpected error: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}
}
