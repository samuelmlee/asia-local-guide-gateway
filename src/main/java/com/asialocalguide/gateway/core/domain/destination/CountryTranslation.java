package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.Language;
import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class CountryTranslation {

  @EmbeddedId @Getter private CountryTranslationId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("countryId")
  @JoinColumn(name = "country_id")
  @Getter
  private Country country;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("languageId")
  @JoinColumn(name = "language_id")
  @Getter
  private Language language;

  @Getter @NotEmpty private String name;

  public CountryTranslation(Country country, Language language, String name) {
    if (country == null) {
      throw new IllegalArgumentException("Country cannot be null");
    }
    if (language == null) {
      throw new IllegalArgumentException("Language cannot be null");
    }

    this.id = new CountryTranslationId();
    this.country = country;
    this.language = language;
    this.name = name;
  }

  void setCountry(Country country) {
    this.country = country;
  }

  @Override
  public String toString() {
    return "CountryTranslation{" + ", id='" + id + '\'' + ", name='" + name + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CountryTranslation that = (CountryTranslation) o;
    return Objects.equal(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
