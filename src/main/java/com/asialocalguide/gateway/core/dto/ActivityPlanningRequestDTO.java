package com.asialocalguide.gateway.core.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ActivityPlanningRequestDTO(
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull Long destinationId,
    List<Long> activityTagIds) {}
