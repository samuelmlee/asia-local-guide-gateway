package com.asialocalguide.gateway.core.validation;

import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RecordValidationInterceptor {

  private static Validator VALIDATOR = null;

  static {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      VALIDATOR = factory.getValidator();
    } catch (ValidationException e) {
      log.error("Failed to create validator", e);
    }
  }

  private RecordValidationInterceptor() {}

  public static <T> void validate(@Origin Constructor<T> constructor, @AllArguments Object[] args) {
    if (VALIDATOR == null) {
      throw new IllegalStateException("Validator is not initialized");
    }

    Set<ConstraintViolation<T>> violations =
        VALIDATOR.forExecutables().validateConstructorParameters(constructor, args);

    if (!violations.isEmpty()) {
      String message =
          violations.stream()
              .map(cv -> cv.getPropertyPath() + " - " + cv.getMessage())
              .collect(Collectors.joining(System.lineSeparator()));

      throw new ConstraintViolationException("Invalid record instantiation: " + message, violations);
    }
  }
}
