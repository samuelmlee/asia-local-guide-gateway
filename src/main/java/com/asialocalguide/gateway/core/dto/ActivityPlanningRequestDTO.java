package com.asialocalguide.gateway.core.dto;

import java.time.Instant;
import java.util.List;

public record ActivityPlanningRequestDTO(
    Instant startDateISO, Instant endDateISO, Long destinationId, List<Long> activityTagIds) {}
