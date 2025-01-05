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

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "destination_id", referencedColumnName = "id")
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

  public DestinationProviderMapping getBookingProviderMapping(BookingProviderName providerName) {
    return bookingProviderMappings.stream()
        .filter(mapping -> mapping.getProviderName().equals(providerName))
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
