package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;

@Embeddable
@Getter
public class DestinationProviderMappingId implements Serializable {
  @Column(name = "destination_id")
  private Long destinationId;

  @Column(name = "booking_provider_id")
  private Long bookingProviderId;

  public DestinationProviderMappingId() {
    // destinationId and bookingProviderId are set by @MapsId in DestinationProviderMapping
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationProviderMappingId that = (DestinationProviderMappingId) o;
    return Objects.equals(destinationId, that.destinationId)
        && Objects.equals(bookingProviderId, that.bookingProviderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(destinationId, bookingProviderId);
  }
}
