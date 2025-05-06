package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.Language;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("languageId")
  @JoinColumn(name = "language_id")
  @Getter
  private Language language;

  @Setter @NotEmpty @Getter private String name;

  public DestinationTranslation(Destination destination, Language language, String name) {
    if (destination == null) {
      throw new IllegalArgumentException("Destination cannot be null");
    }
    if (language == null) {
      throw new IllegalArgumentException("LanguageCode cannot be null");
    }

    this.id = new DestinationTranslationId();
    this.destination = destination;
    this.language = language;
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
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  @Override
  public String toString() {
    return "DestinationTranslation{" + ", id='" + id + '\'' + ", name='" + name + '\'' + '}';
  }
}
