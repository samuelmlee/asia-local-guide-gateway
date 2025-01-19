package com.asialocalguide.gateway.core.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record ActivityPlanningRequestDTO(
    @NotNull Instant startDate,
    @NotNull Instant endDate,
    @NotNull Long destinationId,
    List<Long> activityTagIds) {}
