package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@IdClass(CountryTranslationId.class)
@NoArgsConstructor
public class CountryTranslation {

  @Id @ManyToOne private Country country;

  @Id
  @Enumerated(EnumType.STRING)
  private LanguageCode languageCode;

  private String name;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    CountryTranslation that = (CountryTranslation) o;
    return languageCode.equals(that.languageCode) && name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = 31 * Objects.hashCode(languageCode);
    result = 31 * result + Objects.hashCode(name);
    return result;
  }

  @Override
  public String toString() {
    return "CountryTranslation{"
        + ", languageCode='"
        + languageCode
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
