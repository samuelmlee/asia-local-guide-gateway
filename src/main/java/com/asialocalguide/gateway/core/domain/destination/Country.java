package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Country implements Translatable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "country", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<CountryTranslation> countryTranslations = new HashSet<>();

  @Column(name = "iso_2_code")
  @NotEmpty
  @Length(min = 2, max = 2)
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

  @Override
  public Optional<String> getTranslation(LanguageCode languageCode) {
    if (languageCode == null || countryTranslations == null) {
      return Optional.empty();
    }

    return countryTranslations.stream()
        .filter(ct -> ct.getId().getLanguageCode().equals(languageCode))
        .findFirst()
        .map(CountryTranslation::getName);
  }
}
