package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.*;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class DestinationProviderMapping {

  // TODO: Use composite keys
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "destination_id", nullable = false)
  @Setter
  @NotNull
  private Destination destination;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "provider_id", nullable = false)
  @Setter
  @NotNull
  private BookingProvider provider;

  @Setter @NotNull @NotEmpty private String providerDestinationId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationProviderMapping that = (DestinationProviderMapping) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
