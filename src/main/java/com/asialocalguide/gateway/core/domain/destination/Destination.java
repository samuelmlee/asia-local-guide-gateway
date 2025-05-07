package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
public class Destination extends BaseEntity implements Translatable {

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
  private Set<DestinationProviderMapping> destinationProviderMappings = new HashSet<>();

  @NotNull @Embedded @Getter @Setter Coordinates centerCoordinates;

  @Override
  public Optional<String> getTranslation(LanguageCode languageCode) {
    if (languageCode == null || destinationTranslations.isEmpty()) {
      return Optional.empty();
    }

    return destinationTranslations.stream()
        .filter(t -> t.getId() != null && languageCode.equals(t.getLanguage().getCode()))
        .findFirst()
        .map(DestinationTranslation::getName);
  }

  public void addTranslation(DestinationTranslation translation) {
    if (translation == null) {
      return;
    }
    destinationTranslations.add(translation);
  }

  public int getTranslationCount() {
    if (destinationTranslations == null) {
      return 0;
    }
    return destinationTranslations.size();
  }

  public void addProviderMapping(DestinationProviderMapping mapping) {
    if (mapping == null) {
      return;
    }
    mapping.setDestination(this);
    destinationProviderMappings.add(mapping);
  }

  public void removeProviderMapping(DestinationProviderMapping mapping) {
    if (mapping == null) {
      return;
    }
    mapping.setDestination(null);
    destinationProviderMappings.remove(mapping);
  }

  public Optional<DestinationProviderMapping> getBookingProviderMapping(Long providerId) {
    if (providerId == null || destinationProviderMappings.isEmpty()) {
      return Optional.empty();
    }

    return destinationProviderMappings.stream()
        .filter(
            mapping ->
                mapping.getProvider() != null
                    && mapping.getProvider().getId() != null
                    && providerId.equals(mapping.getProvider().getId()))
        .findFirst();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Destination that = (Destination) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  @Override
  public String toString() {
    return "Destination{" + "id=" + getId() + ", type=" + type + '}';
  }
}
