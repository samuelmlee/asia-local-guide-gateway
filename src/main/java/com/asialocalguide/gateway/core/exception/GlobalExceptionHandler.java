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

/**
 * Global exception handler that maps application exceptions to RFC 9457 {@link ProblemDetail} responses.
 *
 * <p>Handles domain-specific exceptions from the appuser and auth packages, as well as
 * Firebase-specific auth errors, and falls back to {@code 500 Internal Server Error}
 * for unhandled {@link RuntimeException} instances.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * Handles {@link AppUserNotFoundException} with a {@code 404 Not Found} response.
	 *
	 * @param e the exception
	 * @return a problem detail describing the missing user
	 */
	@ExceptionHandler(AppUserNotFoundException.class)
	public ProblemDetail handleUserNotFoundException(AppUserNotFoundException e) {
		log.error("Failed to find user: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	/**
	 * Handles {@link AppUserCreationException} with a {@code 409 Conflict} response.
	 *
	 * @param e the exception
	 * @return a problem detail describing the creation failure
	 */
	@ExceptionHandler(AppUserCreationException.class)
	public ProblemDetail handleUserCreationException(AppUserCreationException e) {
		log.error("Failed to create user: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
	}

	/**
	 * Handles {@link ProviderUserDeletionException} with {@code 400} for validation errors
	 * or {@code 500} for other failure types.
	 *
	 * @param e the exception
	 * @return a problem detail with the appropriate status
	 */
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

	/**
	 * Handles {@link AuthProviderException} with {@code 404} when the Firebase user is not found,
	 * or {@code 500} for all other provider errors.
	 *
	 * @param e the exception
	 * @return a problem detail with the appropriate status
	 */
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

	/**
	 * Catch-all handler for unhandled {@link RuntimeException} instances,
	 * returning {@code 500 Internal Server Error}.
	 *
	 * @param e the exception
	 * @return a generic internal error problem detail
	 */
	@ExceptionHandler(RuntimeException.class)
	public ProblemDetail handleRuntimeException(RuntimeException e) {
		log.error("Unexpected error: {}", e.getMessage(), e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}
}
