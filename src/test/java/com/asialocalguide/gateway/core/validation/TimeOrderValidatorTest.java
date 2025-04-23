package com.asialocalguide.gateway.core.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeOrderValidatorTest {

  private TimeOrderValidator validator;

  @Mock private ConstraintValidatorContext context;

  @Mock private ValidTimeOrder annotation;

  @BeforeEach
  void setUp() {
    validator = new TimeOrderValidator();
    when(annotation.startTimeField()).thenReturn("startTime");
    when(annotation.endTimeField()).thenReturn("endTime");
    validator.initialize(annotation);
  }

  @Test
  void shouldValidateWhenEndTimeIsAfterStartTime() {
    // Given
    TestClass testObject = new TestClass(LocalDateTime.of(2023, 1, 1, 10, 0), LocalDateTime.of(2023, 1, 1, 12, 0));

    // When
    boolean result = validator.isValid(testObject, context);

    // Then
    assertTrue(result);
  }

  @Test
  void shouldValidateWhenTimesAreEqual() {
    // Given
    LocalDateTime sameTime = LocalDateTime.of(2023, 1, 1, 12, 0);
    TestClass testObject = new TestClass(sameTime, sameTime);

    // When
    boolean result = validator.isValid(testObject, context);

    // Then
    assertTrue(result);
  }

  @Test
  void shouldNotValidateWhenEndTimeIsBeforeStartTime() {
    // Given
    TestClass testObject = new TestClass(LocalDateTime.of(2023, 1, 1, 12, 0), LocalDateTime.of(2023, 1, 1, 10, 0));

    // When
    boolean result = validator.isValid(testObject, context);

    // Then
    assertFalse(result);
  }

  @Test
  void shouldNotValidateWhenTimeValuesAreNull() {
    // Given
    TestClass testObject = new TestClass(null, null);

    // When
    boolean result = validator.isValid(testObject, context);

    // Then
    assertFalse(result);
  }

  @Test
  void shouldHandleMissingFields() {
    // Given
    Object invalidObject = new Object();

    // When
    boolean result = validator.isValid(invalidObject, context);

    // Then
    assertFalse(result);
  }

  // Simple class that mimics entities with start/end times
  static class TestClass {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    TestClass(LocalDateTime startTime, LocalDateTime endTime) {
      this.startTime = startTime;
      this.endTime = endTime;
    }
  }
}
