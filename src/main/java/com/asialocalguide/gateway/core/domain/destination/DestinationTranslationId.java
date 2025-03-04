package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class DestinationTranslationId {
  @Column(name = "destination_id")
  private Long destinationId;

  @Column(name = "language_code")
  @Convert(converter = LanguageCodeConverter.class)
  private LanguageCode languageCode;
}
