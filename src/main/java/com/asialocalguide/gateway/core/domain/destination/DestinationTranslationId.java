package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class DestinationTranslationId implements Serializable {
  @Column(name = "destination_id")
  private Long destinationId;

  @Column(name = "language_code")
  @Convert(converter = LanguageCodeConverter.class)
  private LanguageCode languageCode;

  public DestinationTranslationId(LanguageCode languageCode) {
    // destinationId is set by Hibernate with @MapsId in DestinationTranslation
    this.languageCode = languageCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationTranslationId that = (DestinationTranslationId) o;
    return Objects.equals(destinationId, that.destinationId) && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(destinationId, languageCode);
  }
}
