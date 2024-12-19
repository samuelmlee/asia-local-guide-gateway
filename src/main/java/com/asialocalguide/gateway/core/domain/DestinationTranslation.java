package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@IdClass(DestinationTranslationId.class)
@NoArgsConstructor
public class DestinationTranslation {

  @Id private Long id;

  @Id private String locale;

  private String destinationName;

  public DestinationTranslation(String locale, String destinationName) {
    this.locale = locale;
    this.destinationName = destinationName;
  }
}
