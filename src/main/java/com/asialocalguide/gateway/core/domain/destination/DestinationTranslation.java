package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@IdClass(DestinationTranslationId.class)
@NoArgsConstructor
public class DestinationTranslation {

  @Id @ManyToOne private Destination destination;

  @Id private String languageCode;

  private String destinationName;

  public DestinationTranslation(String languageCode, String destinationName) {
    this.languageCode = languageCode;
    this.destinationName = destinationName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DestinationTranslation that = (DestinationTranslation) o;
    return languageCode.equals(that.languageCode) && destinationName.equals(that.destinationName);
  }

  @Override
  public int hashCode() {
    int result = 31 * Objects.hashCode(languageCode);
    result = 31 * result + Objects.hashCode(destinationName);
    return result;
  }

  @Override
  public String toString() {
    return "DestinationTranslation{"
        + ", languageCode='"
        + languageCode
        + '\''
        + ", destinationName='"
        + destinationName
        + '\''
        + '}';
  }
}
