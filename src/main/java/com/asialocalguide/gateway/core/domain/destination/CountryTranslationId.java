package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Embeddable
@Getter
public class CountryTranslationId implements Serializable {
  @Column(name = "country_id")
  private Long countryId;

  @Column(name = "language_code")
  @Convert(converter = LanguageCodeConverter.class)
  private LanguageCode languageCode;

  public CountryTranslationId(LanguageCode languageCode) {
    // countryId is set by Hibernate with @MapsId in CountryTranslation
    this.languageCode = languageCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CountryTranslationId that = (CountryTranslationId) o;
    return Objects.equals(countryId, that.countryId) && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(countryId, languageCode);
  }

  @Override
  public String toString() {
    return "CountryTranslationId{" + "countryId=" + countryId + ", languageCode=" + languageCode + '}';
  }
}
