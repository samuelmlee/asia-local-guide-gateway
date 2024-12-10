package com.asialocalguide.gateway.dto;

import com.asialocalguide.gateway.entity.DestinationType;

public record DestinationDTO(Long destinationId, String name, DestinationType type) {
  public static DestinationDTO of(Long destinationId, String name, DestinationType type) {
    return new DestinationDTO(destinationId, name, type);
  }
}
