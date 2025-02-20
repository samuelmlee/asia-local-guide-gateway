package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class DestinationTranslation {

  @EmbeddedId private DestinationTranslationId id;

  @ManyToOne
  @MapsId("destinationId")
  @JoinColumn(name = "destination_id")
  private Destination destination;

  private String name;

  public DestinationTranslation(Destination destination, LanguageCode languageCode, String name) {
    this.id = new DestinationTranslationId();
    this.id.setLanguageCode(languageCode);
    this.destination = destination;
    this.name = name;
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
