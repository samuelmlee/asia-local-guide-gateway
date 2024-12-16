package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class Destination {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id", referencedColumnName = "id")
  private List<DestinationTranslation> destinationTranslations;

  private DestinationType type;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "destination_id", referencedColumnName = "id")
  private List<BookingProviderMapping> bookingProviderMappings;
}
