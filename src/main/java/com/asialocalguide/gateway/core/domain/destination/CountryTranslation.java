package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@IdClass(CountryTranslationId.class)
@NoArgsConstructor
public class CountryTranslation {

  @Id @ManyToOne private Country country;

  @Id private String languageCode;

  private String countryName;

  public CountryTranslation(String languageCode, String countryName) {
    this.languageCode = languageCode;
    this.countryName = countryName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    CountryTranslation that = (CountryTranslation) o;
    return languageCode.equals(that.languageCode) && countryName.equals(that.countryName);
  }

  @Override
  public int hashCode() {
    int result = 31 * Objects.hashCode(languageCode);
    result = 31 * result + Objects.hashCode(countryName);
    return result;
  }

  @Override
  public String toString() {
    return "CountryTranslation{"
        + ", languageCode='"
        + languageCode
        + '\''
        + ", countryName='"
        + countryName
        + '\''
        + '}';
  }
}
