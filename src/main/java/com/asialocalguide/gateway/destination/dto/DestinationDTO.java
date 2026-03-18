package com.asialocalguide.gateway.destination.dto;

import java.util.UUID;

import com.asialocalguide.gateway.destination.domain.DestinationType;

/**
 * Data transfer object representing a destination for API responses.
 *
 * @param destinationId the unique identifier of the destination
 * @param name          the localized display name of the destination
 * @param type          the geographic classification of the destination
 * @param parentName    the localized name of the parent entity (e.g. country)
 */
public record DestinationDTO(UUID destinationId, String name, DestinationType type, String parentName) {

	/**
	 * Convenience factory method for creating a {@link DestinationDTO}.
	 *
	 * @param destinationId the destination's unique identifier
	 * @param name          the localized name
	 * @param type          the destination type
	 * @param parentName    the parent entity's localized name
	 * @return a new {@link DestinationDTO} instance
	 */
	public static DestinationDTO of(UUID destinationId, String name, DestinationType type, String parentName) {
		return new DestinationDTO(destinationId, name, type, parentName);
	}
}
