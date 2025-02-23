package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class CountryTranslation {

  @EmbeddedId private CountryTranslationId id;

  @ManyToOne
  @MapsId("countryId")
  @JoinColumn(name = "country_id")
  private Country country;

  @NotEmpty private String name;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    CountryTranslation that = (CountryTranslation) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return 31 * Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "CountryTranslation{" + ", languageCode='" + id.getLanguageCode() + '\'' + ", name='" + name + '\'' + '}';
  }
}
