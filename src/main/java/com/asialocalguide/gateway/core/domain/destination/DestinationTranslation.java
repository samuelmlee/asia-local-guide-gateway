package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@IdClass(DestinationTranslationId.class)
@NoArgsConstructor
public class DestinationTranslation {

  @Id @ManyToOne private Destination destination;

  @Id
  @Enumerated(EnumType.STRING)
  private LanguageCode languageCode;

  private String name;

  public DestinationTranslation(LanguageCode code, String name) {
    this.languageCode = code;
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DestinationTranslation that = (DestinationTranslation) o;
    return languageCode.equals(that.languageCode) && name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = 31 * Objects.hashCode(languageCode);
    result = 31 * result + Objects.hashCode(name);
    return result;
  }

  @Override
  public String toString() {
    return "DestinationTranslation{" + ", languageCode='" + languageCode + '\'' + ", names='" + name + '\'' + '}';
  }
}
