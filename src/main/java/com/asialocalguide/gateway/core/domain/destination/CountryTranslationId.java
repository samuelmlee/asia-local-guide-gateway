package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
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

  @Column(name = "language_id")
  private Long languageId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CountryTranslationId that = (CountryTranslationId) o;
    return Objects.equals(getCountryId(), that.getCountryId()) && Objects.equals(getLanguageId(), that.getLanguageId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCountryId(), getLanguageId());
  }

  @Override
  public String toString() {
    return "CountryTranslationId{" + "countryId=" + getCountryId() + ", languageId=" + getLanguageId() + '}';
  }
}
