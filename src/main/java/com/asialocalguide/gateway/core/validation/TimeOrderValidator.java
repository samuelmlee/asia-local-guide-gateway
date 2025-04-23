package com.asialocalguide.gateway.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class TimeOrderValidator implements ConstraintValidator<ValidTimeOrder, Object> {
  private String startTimeFieldName;
  private String endTimeFieldName;

  @Override
  public void initialize(ValidTimeOrder constraintAnnotation) {
    startTimeFieldName = constraintAnnotation.startTimeField();
    endTimeFieldName = constraintAnnotation.endTimeField();
  }

  @Override
  @SuppressWarnings("java:S3011")
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    try {
      final Field startTimeField = object.getClass().getDeclaredField(startTimeFieldName);
      startTimeField.setAccessible(true);
      final LocalDateTime startTime = (LocalDateTime) startTimeField.get(object);

      final Field endTimeField = object.getClass().getDeclaredField(endTimeFieldName);
      endTimeField.setAccessible(true);
      final LocalDateTime endTime = (LocalDateTime) endTimeField.get(object);

      if (startTime == null || endTime == null) {
        return false;
      }

      return startTime.isBefore(endTime) || startTime.isEqual(endTime);

    } catch (NoSuchFieldException | IllegalAccessException ignored) {
      return false;
    }
  }
}
