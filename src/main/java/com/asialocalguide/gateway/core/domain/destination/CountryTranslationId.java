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
public class CountryTranslationId {
  @Column(name = "country_id")
  private Long countryId;

  @Column(name = "language_code")
  @Convert(converter = LanguageCodeConverter.class)
  private LanguageCode languageCode;
}
