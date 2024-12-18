package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Destination {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id", referencedColumnName = "id")
  private List<DestinationTranslation> destinationTranslations;

  @Enumerated(EnumType.STRING)
  private DestinationType type;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "destination_id", referencedColumnName = "id")
  private List<BookingProviderMapping> bookingProviderMappings;
}
