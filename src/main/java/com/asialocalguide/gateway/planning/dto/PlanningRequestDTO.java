package com.asialocalguide.gateway.planning.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for generating a day-plan schedule.
 *
 * @param startDate      first day of the planning period; must not be {@code null}
 * @param endDate        last day of the planning period; must not be {@code null}
 * @param destinationId  UUID of the target destination; must not be {@code null}
 * @param activityTagIds optional list of activity tag IDs to filter results
 */
public record PlanningRequestDTO(@NotNull LocalDate startDate, @NotNull LocalDate endDate, @NotNull UUID destinationId,
		List<String> activityTagIds) {

	/**
	 * Returns the inclusive number of days in the planning period.
	 *
	 * @return duration in days (at least 1)
	 */
	public int getDuration() {
		return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
	}
}
