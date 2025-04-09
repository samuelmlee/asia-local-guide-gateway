package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Destination implements Translatable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_id")
  @NotNull
  @Getter
  @Setter
  private Country country;

  @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<DestinationTranslation> destinationTranslations = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @NotNull
  @Getter
  @Setter
  private DestinationType type;

  @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<DestinationProviderMapping> destinationProviderMappings = new HashSet<>();

  @NotNull @Embedded @Getter @Setter Coordinates centerCoordinates;

  @Override
  public Optional<String> getTranslation(LanguageCode languageCode) {
    return destinationTranslations.stream()
        .filter(t -> t.getId().getLanguageCode().equals(languageCode))
        .findFirst()
        .map(DestinationTranslation::getName);
  }

  public void addTranslation(DestinationTranslation translation) {
    translation.setDestination(this);
    destinationTranslations.add(translation);
  }

  public void removeTranslation(DestinationTranslation translation) {
    if (destinationTranslations != null) {
      translation.setDestination(null);
      destinationTranslations.remove(translation);
    }
  }

  public int getTranslationCount() {
    if (destinationTranslations == null) {
      return 0;
    }
    return destinationTranslations.size();
  }

  public void addProviderMapping(DestinationProviderMapping mapping) {
    mapping.setDestination(this);
    destinationProviderMappings.add(mapping);
  }

  public void removeProviderMapping(DestinationProviderMapping mapping) {
    if (mapping != null) {
      mapping.setDestination(null);
      destinationProviderMappings.remove(mapping);
    }
  }

  public Optional<DestinationProviderMapping> getBookingProviderMapping(Long providerId) {
    return destinationProviderMappings.stream()
        .filter(mapping -> mapping.getProvider().getId().equals(providerId))
        .findFirst();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    Destination that = (Destination) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return "Destination{" + "id=" + id + ", type=" + type + '}';
  }
}
