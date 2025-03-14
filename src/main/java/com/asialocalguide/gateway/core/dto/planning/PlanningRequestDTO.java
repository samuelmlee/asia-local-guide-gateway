package com.asialocalguide.gateway.core.dto.planning;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record PlanningRequestDTO(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Long destinationId,
        List<String> activityTagIds) {

    public int getDuration() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

}
