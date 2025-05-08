package com.asialocalguide.gateway.core.dto.destination;

import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import java.util.UUID;

public record DestinationDTO(UUID destinationId, String name, DestinationType type, String parentName) {

  public static DestinationDTO of(UUID destinationId, String name, DestinationType type, String parentName) {
    return new DestinationDTO(destinationId, name, type, parentName);
  }
}
