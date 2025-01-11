package com.asialocalguide.gateway.viator.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ViatorActivitySortingOrder {
  ASCENDING("ASCENDING"),
  DESCENDING("DESCENDING");

  private final String value;

  ViatorActivitySortingOrder(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return this.value;
  }
}
