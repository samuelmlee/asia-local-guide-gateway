package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ViatorActivitySortingType {
  DEFAULT,
  PRICE,
  TRAVELER_RATING,
  ITINERARY_DURATION,
  DATE_ADDED;

  @JsonValue
  public String getValue() {
    return this.name();
  }
}
