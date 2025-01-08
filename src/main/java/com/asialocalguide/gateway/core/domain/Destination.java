package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Destination {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_destination_id")
  private Destination parentDestination;

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
  private Set<DestinationProviderMapping> bookingProviderMappings = new HashSet<>();

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
    bookingProviderMappings.add(mapping);
  }

  public void removeProviderMapping(DestinationProviderMapping mapping) {
    if (mapping != null) {
      mapping.setDestination(null);
      bookingProviderMappings.remove(mapping);
    }
  }

  public DestinationProviderMapping getBookingProviderMapping(Long providerId) {
    return bookingProviderMappings.stream()
        .filter(mapping -> mapping.getProvider().getId().equals(providerId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(id);
    result = 31 * result + Objects.hashCode(type);
    result = 31 * result + Objects.hashCode(bookingProviderMappings);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    Destination that = (Destination) o;
    return id.equals(that.id) && type == that.type;
  }

  @Override
  public String toString() {
    return "Destination{"
        + "id="
        + id
        + ", type="
        + type
        + ", bookingProviderMappings="
        + bookingProviderMappings
        + '}';
  }
}
