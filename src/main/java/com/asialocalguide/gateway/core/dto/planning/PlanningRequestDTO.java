package com.asialocalguide.gateway.core.dto.planning;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public record PlanningRequestDTO(
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull UUID destinationId,
    List<String> activityTagIds) {

  public int getDuration() {
    return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
  }
}
