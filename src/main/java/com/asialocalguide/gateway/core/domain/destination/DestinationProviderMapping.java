package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class DestinationProviderMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "destination_id", nullable = false)
  @Setter
  private Destination destination;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "provider_id", nullable = false)
  @Setter
  private BookingProvider provider;

  @Setter @NotNull @NotEmpty private String providerDestinationId;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DestinationProviderMapping mapping = (DestinationProviderMapping) o;
    return provider.equals(mapping.provider) && providerDestinationId.equals(mapping.providerDestinationId);
  }

  @Override
  public int hashCode() {
    return providerDestinationId.hashCode();
  }
}
