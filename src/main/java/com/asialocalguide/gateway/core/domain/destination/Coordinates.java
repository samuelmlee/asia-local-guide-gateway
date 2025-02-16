package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Coordinates {

  private Double latitude;
  private Double longitude;

  public Coordinates(Double latitude, Double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

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
