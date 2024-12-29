package com.asialocalguide.gateway.core.dto;

import com.asialocalguide.gateway.core.domain.DestinationType;

public record DestinationDTO(
    Long destinationId, String name, DestinationType type, String parentName) {

  public static DestinationDTO of(
      Long destinationId, String name, DestinationType type, String parentName) {
    return new DestinationDTO(destinationId, name, type, parentName);
  }
}
