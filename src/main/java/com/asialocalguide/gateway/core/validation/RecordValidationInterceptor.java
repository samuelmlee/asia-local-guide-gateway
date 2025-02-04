package com.asialocalguide.gateway.core.validation;

import jakarta.validation.*;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Collectors;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;

public class RecordValidationInterceptor {

  private static final Validator VALIDATOR;

  static {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    VALIDATOR = factory.getValidator();
  }

  public static <T> void validate(@Origin Constructor<T> constructor, @AllArguments Object[] args) {
    Set<ConstraintViolation<T>> violations =
        VALIDATOR.forExecutables().validateConstructorParameters(constructor, args);

    if (!violations.isEmpty()) {
      String message =
          violations.stream()
              .map(cv -> cv.getPropertyPath() + " - " + cv.getMessage())
              .collect(Collectors.joining(System.lineSeparator()));

      throw new ConstraintViolationException(
          "Invalid record instantiation: " + message, violations);
    }
  }
}
