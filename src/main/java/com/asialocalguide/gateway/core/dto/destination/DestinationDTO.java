package com.asialocalguide.gateway.core.dto.destination;

import com.asialocalguide.gateway.core.domain.destination.DestinationType;

public record DestinationDTO(
    Long destinationId, String name, DestinationType type, String parentName) {

  public static DestinationDTO of(
      Long destinationId, String name, DestinationType type, String parentName) {
    return new DestinationDTO(destinationId, name, type, parentName);
  }
}
