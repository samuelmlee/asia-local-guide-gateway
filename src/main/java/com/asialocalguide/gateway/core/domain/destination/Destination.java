package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
public class Destination implements Translatable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "country_id")
  @NotNull
  @Getter
  @Setter
  private Country country;

  @OneToMany(mappedBy = "destination", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private Set<DestinationTranslation> destinationTranslations = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @NotNull
  @Getter
  @Setter
  private DestinationType type;

  @OneToMany(mappedBy = "destination", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
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

  public DestinationProviderMapping getBookingProviderMapping(Long providerId) {
    return destinationProviderMappings.stream()
        .filter(mapping -> mapping.getProvider().getId().equals(providerId))
        .findFirst()
        .orElse(null);
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
