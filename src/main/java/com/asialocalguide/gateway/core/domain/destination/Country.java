package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Data;

@Entity
@Data
public class Country implements Translatable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(
      mappedBy = "country",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<CountryTranslation> countryTranslations = new HashSet<>();

  @NotEmpty private String callingCode;

  @Column(name = "iso_2_code")
  @NotEmpty
  private String iso2Code;

  public void addTranslation(CountryTranslation translation) {
    if (translation == null) {
      return;
    }
    translation.setCountry(this);
    countryTranslations.add(translation);
  }

  public void removeTranslation(CountryTranslation translation) {
    if (countryTranslations == null) {
      return;
    }
    translation.setCountry(null);
    countryTranslations.remove(translation);
  }

  @Override
  public Optional<String> getTranslation(LanguageCode languageCode) {
    return countryTranslations.stream()
        .filter(ct -> ct.getLanguageCode().equals(languageCode))
        .findFirst()
        .map(CountryTranslation::getName);
  }
}
