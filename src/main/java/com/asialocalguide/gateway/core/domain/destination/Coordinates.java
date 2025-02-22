package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class Coordinates {

  @NotNull
  @DecimalMin(value = "-90.0")
  @DecimalMax(value = "90.0")
  private Double latitude;

  @NotNull
  @DecimalMin(value = "-180.0")
  @DecimalMax(value = "180.0")
  private Double longitude;

  public Coordinates(Double latitude, Double longitude) {
    if (latitude == null) {
      throw new IllegalArgumentException("Latitude cannot be null");
    }
    if (longitude == null) {
      throw new IllegalArgumentException("Longitude cannot be null");
    }
    if (latitude < -90.0 || latitude > 90.0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90");
    }
    if (longitude < -180.0 || longitude > 180.0) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180");
    }

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
