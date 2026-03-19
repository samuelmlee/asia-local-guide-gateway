package com.asialocalguide.gateway.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint annotation that validates chronological ordering of two
 * {@link java.time.LocalDateTime} fields within the annotated type.
 *
 * <p>Applied to classes or records that carry a start and end time, ensuring the start
 * is before or equal to the end. Field names default to {@code startTime} and {@code endTime}
 * but can be overridden via {@link #startTimeField()} and {@link #endTimeField()}.
 *
 * @see TimeOrderValidator
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeOrderValidator.class)
public @interface ValidTimeOrder {
	String message() default "End time must be after start time";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String startTimeField() default "startTime";

	String endTimeField() default "endTime";
}
