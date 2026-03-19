package com.asialocalguide.gateway.appuser.dto;

import java.util.UUID;

/**
 * Data transfer object representing an application user in API responses.
 *
 * @param userId the unique identifier of the application user
 */
public record AppUserDTO(UUID userId) {
}
