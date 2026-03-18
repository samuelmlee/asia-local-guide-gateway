package com.asialocalguide.gateway.core.validation;

import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ByteBuddy interceptor that runs Jakarta Bean Validation on record constructor parameters
 * at instantiation time.
 *
 * <p>Invoked by {@link RecordValidationPlugin} after the canonical record constructor completes.
 * Throws {@link jakarta.validation.ConstraintViolationException} if any constraint is violated.
 */
@Slf4j
public class RecordValidationInterceptor {

	private static Validator validator = null;

	static {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		} catch (ValidationException e) {
			log.error("Failed to create validator", e);
		}
	}

	private RecordValidationInterceptor() {
	}

	/**
	 * Validates the given constructor arguments and throws if any constraint is violated.
	 *
	 * @param <T>         the record type being constructed
	 * @param constructor the canonical constructor of the record
	 * @param args        the constructor arguments to validate
	 * @throws jakarta.validation.ConstraintViolationException if one or more constraints fail
	 * @throws IllegalStateException                           if the validator could not be initialised
	 */
	public static <T> void validate(@Origin Constructor<T> constructor, @AllArguments Object[] args) {
		if (validator == null) {
			throw new IllegalStateException("Validator is not initialized");
		}

		Set<ConstraintViolation<T>> violations = validator.forExecutables()
				.validateConstructorParameters(constructor, args);

		if (!violations.isEmpty()) {
			String message = violations.stream()
					.map(cv -> cv.getPropertyPath() + " - " + cv.getMessage())
					.collect(Collectors.joining(System.lineSeparator()));

			throw new ConstraintViolationException("Invalid record instantiation: " + message, violations);
		}
	}
}
