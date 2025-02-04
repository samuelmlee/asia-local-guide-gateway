package com.asialocalguide.gateway.core.domain;

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

  @Id private String locale;

  private String destinationName;

  public DestinationTranslation(String locale, String destinationName) {
    this.locale = locale;
    this.destinationName = destinationName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DestinationTranslation that = (DestinationTranslation) o;
    return locale.equals(that.locale) && destinationName.equals(that.destinationName);
  }

  @Override
  public int hashCode() {
    int result = 31 * Objects.hashCode(locale);
    result = 31 * result + Objects.hashCode(destinationName);
    return result;
  }

  @Override
  public String toString() {
    return "DestinationTranslation{"
        + ", locale='"
        + locale
        + '\''
        + ", destinationName='"
        + destinationName
        + '\''
        + '}';
  }
}
