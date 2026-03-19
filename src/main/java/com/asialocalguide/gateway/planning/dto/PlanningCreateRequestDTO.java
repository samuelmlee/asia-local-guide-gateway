package com.asialocalguide.gateway.planning.dto;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for persisting a confirmed planning.
 *
 * @param name     the name to give the planning; must not be blank
 * @param dayPlans the list of day plans to save; must not be empty
 */
public record PlanningCreateRequestDTO(@NotBlank String name, @NotEmpty List<CreateDayPlanDTO> dayPlans) {

	/**
	 * @param date       the calendar date for this day plan ({@code yyyy-MM-dd}); must not be {@code null}
	 * @param activities the activities scheduled for this day; must not be empty
	 */
	public record CreateDayPlanDTO(
			@NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
			@NotEmpty List<CreateDayActivityDTO> activities) {
	}

	/**
	 * @param productCode          the provider's activity identifier; must not be blank
	 * @param bookingProviderName  the provider supplying this activity; must not be {@code null}
	 * @param startTime            scheduled start ({@code yyyy-MM-dd'T'HH:mm}); must not be {@code null}
	 * @param endTime              scheduled end ({@code yyyy-MM-dd'T'HH:mm}); must not be {@code null}
	 */
	public record CreateDayActivityDTO(@NotBlank String productCode, @NotNull BookingProviderName bookingProviderName,
			@NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
			@NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime) {
	}
}
