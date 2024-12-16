package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@IdClass(DestinationTranslationId.class)
@Builder
public class DestinationTranslation {

  @Id private Long id;

  @Id private String locale;

  private String destinationName;
}
