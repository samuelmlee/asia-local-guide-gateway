package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Coordinates {

  private Double latitude;
  private Double longitude;
}
