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

  @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DestinationTranslation> destinationTranslations;

  private DestinationType type;
}
