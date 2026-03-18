package com.asialocalguide.gateway.planning.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing a single day's plan, containing the date and its scheduled activities.
 *
 * @param date       the calendar date for this day ({@code yyyy-MM-dd})
 * @param activities ordered list of activities scheduled for this day
 */
public record DayPlanDTO(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
		List<DayActivityDTO> activities) {
}
