package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(DestinationTranslationId.class)
public class DestinationTranslation {

  @Id private Long id;

  @Id private String locale;

  private String destinationName;
}
