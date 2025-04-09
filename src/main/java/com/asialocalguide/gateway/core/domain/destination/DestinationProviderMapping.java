package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.*;

@Entity
@NoArgsConstructor
public class DestinationProviderMapping {

  @EmbeddedId private DestinationProviderMappingId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("destinationId")
  @JoinColumn(name = "destination_id", nullable = false)
  @NotNull
  @Getter
  private Destination destination;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookingProviderId")
  @JoinColumn(name = "booking_provider_id", nullable = false)
  @NotNull
  @Getter
  private BookingProvider provider;

  @NotEmpty @Getter private String providerDestinationId;

  public DestinationProviderMapping(Destination destination, BookingProvider provider, String providerDestinationId) {
    if (destination == null) {
      throw new IllegalArgumentException("Destination cannot be null");
    }
    if (provider == null) {
      throw new IllegalArgumentException("BookingProvider cannot be null");
    }

    this.id = new DestinationProviderMappingId();
    this.destination = destination;
    this.provider = provider;
    this.providerDestinationId = providerDestinationId;
  }

  void setDestination(Destination destination) {
    this.destination = destination;
  }

  void setProvider(BookingProvider provider) {
    this.provider = provider;
  }

  void setProviderDestinationId(String providerDestinationId) {
    this.providerDestinationId = providerDestinationId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationProviderMapping that = (DestinationProviderMapping) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
