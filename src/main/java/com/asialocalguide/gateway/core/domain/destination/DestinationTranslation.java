package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
public class DestinationTranslation {

  @EmbeddedId @Getter private DestinationTranslationId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("destinationId")
  @JoinColumn(name = "destination_id")
  @Getter
  private Destination destination;

  @Setter @NotEmpty @Getter private String name;

  public DestinationTranslation(Destination destination, LanguageCode languageCode, String name) {
    if (destination == null) {
      throw new IllegalArgumentException("Destination cannot be null");
    }
    if (languageCode == null) {
      throw new IllegalArgumentException("LanguageCode cannot be null");
    }

    this.id = new DestinationTranslationId(languageCode);
    this.destination = destination;
    this.name = name;
  }

  void setDestination(Destination destination) {
    this.destination = destination;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationTranslation that = (DestinationTranslation) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "DestinationTranslation{"
        + ", languageCode='"
        + id.getLanguageCode()
        + '\''
        + ", name='"
        + name
        + '\''
        + '}';
  }
}
