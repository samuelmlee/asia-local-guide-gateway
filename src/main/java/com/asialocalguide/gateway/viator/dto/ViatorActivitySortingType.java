package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ViatorActivitySortingType {
  DEFAULT("DEFAULT"),
  PRICE("PRICE"),
  TRAVELER_RATING("TRAVELER_RATING"),
  ITINERARY_DURATION("ITINERARY_DURATION"),
  DATE_ADDED("DATE_ADDED");

  private final String value;

  ViatorActivitySortingType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return this.value;
  }
}
