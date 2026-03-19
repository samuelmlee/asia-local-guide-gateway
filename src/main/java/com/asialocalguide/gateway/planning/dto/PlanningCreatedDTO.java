package com.asialocalguide.gateway.planning.dto;

import java.util.UUID;

/**
 * Response DTO returned after a planning is successfully persisted.
 *
 * @param planningId the UUID of the newly created planning
 */
public record PlanningCreatedDTO(UUID planningId) {
}
