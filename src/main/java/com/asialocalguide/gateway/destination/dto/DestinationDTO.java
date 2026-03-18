package com.asialocalguide.gateway.destination.dto;

import java.util.UUID;

import com.asialocalguide.gateway.destination.domain.DestinationType;

public record DestinationDTO(UUID destinationId, String name, DestinationType type, String parentName) {

	public static DestinationDTO of(UUID destinationId, String name, DestinationType type, String parentName) {
		return new DestinationDTO(destinationId, name, type, parentName);
	}
}
