package com.asialocalguide.gateway.core.domain.destination;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Entity
@NoArgsConstructor
public class Country implements Translatable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<CountryTranslation> countryTranslations = new HashSet<>();

  @Column(name = "iso_2_code")
  @NotEmpty
  @Length(min = 2, max = 2)
  @Getter
  @Setter
  private String iso2Code;

  public Country(String iso2Code) {
    if (iso2Code == null || iso2Code.length() != 2) {
      throw new IllegalArgumentException("ISO2 code must be 2 characters long");
    }

    this.iso2Code = iso2Code;
  }

  public void addTranslation(CountryTranslation translation) {
    if (translation == null) {
      throw new IllegalArgumentException("Translation cannot be null");
    }
    if (countryTranslations == null) {
      countryTranslations = new HashSet<>();
    }
    translation.setCountry(this);
    countryTranslations.add(translation);
  }

  public void removeTranslation(CountryTranslation translation) {
    if (translation == null) {
      throw new IllegalArgumentException("Translation cannot be null");
    }

    if (countryTranslations == null) {
      return;
    }
    translation.setCountry(null);
    countryTranslations.remove(translation);
  }

  /*
   * Method needs the Language entity in CountryTranslation to be eagerly loaded.
   */
  @Override
  public Optional<String> getTranslation(LanguageCode languageCode) {
    if (languageCode == null || countryTranslations == null) {
      return Optional.empty();
    }

    return countryTranslations.stream()
        .filter(ct -> ct.getId() != null && languageCode.equals(ct.getLanguage().getCode()))
        .findFirst()
        .map(CountryTranslation::getName);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Country country = (Country) o;
    return Objects.equal(id, country.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
