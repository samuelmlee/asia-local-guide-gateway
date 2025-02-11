package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Coordinates {

  private Double latitude;
  private Double longitude;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    Coordinates that = (Coordinates) o;
    return latitude.equals(that.latitude) && longitude.equals(that.longitude);
  }

  @Override
  public int hashCode() {
    int result = latitude.hashCode();
    result = 31 * result + longitude.hashCode();
    return result;
  }
}
