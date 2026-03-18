package com.asialocalguide.gateway.planning.dto;

import java.util.UUID;

/**
 * A simple DTO for summarizing planning information
 *
 * @param id   the unique identifier of the planning
 * @param name the name of the planning
 */
public record PlanningSummaryDTO(UUID id, String name) {
}
