package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class DestinationProviderMappingId implements Serializable {
  @Column(name = "destination_id")
  private UUID destinationId;

  @Column(name = "booking_provider_id")
  private Long bookingProviderId;

  public DestinationProviderMappingId(UUID destinationId, Long bookingProviderId) {
    if (destinationId == null || bookingProviderId == null) {
      throw new IllegalArgumentException(
          String.format("DestinationId: %s or BookingProviderId: %s cannot be null", destinationId, bookingProviderId));
    }
    this.destinationId = destinationId;
    this.bookingProviderId = bookingProviderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationProviderMappingId that = (DestinationProviderMappingId) o;
    return Objects.equals(getDestinationId(), that.getDestinationId())
        && Objects.equals(getBookingProviderId(), that.getBookingProviderId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDestinationId(), getBookingProviderId());
  }
}
