package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Destination implements Translatable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "country_id")
  private Country country;

  @OneToMany(
      mappedBy = "destination",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<DestinationTranslation> destinationTranslations = new HashSet<>();

  @Enumerated(EnumType.STRING)
  private DestinationType type;

  @OneToMany(
      mappedBy = "destination",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<DestinationProviderMapping> destinationProviderMappings = new HashSet<>();

  @Embedded Coordinates coordinates;

  @Override
  public Optional<String> getTranslation(LanguageCode languageCode) {
    return destinationTranslations.stream()
        .filter(t -> t.getLanguageCode().equals(languageCode.getCode()))
        .findFirst()
        .map(DestinationTranslation::getDestinationName);
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
    return Objects.equals(id, that.id)
        && Objects.equals(coordinates, that.coordinates)
        && type == that.type;
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
