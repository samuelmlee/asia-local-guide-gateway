package com.asialocalguide.gateway.core.dto;

import java.time.Instant;
import java.util.List;

public record ActivityPlanningRequestDTO(
    Instant startDate, Instant endDate, Long destinationId, List<Long> activityTagIds) {}
