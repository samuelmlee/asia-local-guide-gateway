package com.asialocalguide.gateway.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeOrderValidator.class)
public @interface ValidTimeOrder {
  String message() default "End time must be after start time";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String startTimeField() default "startTime";

  String endTimeField() default "endTime";
}
