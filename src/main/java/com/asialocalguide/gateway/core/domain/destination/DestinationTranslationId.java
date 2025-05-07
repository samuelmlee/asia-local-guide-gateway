package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class DestinationTranslationId implements Serializable {
  @Column(name = "destination_id")
  private UUID destinationId;

  @Column(name = "language_id")
  private Long languageId;

  public DestinationTranslationId(UUID destinationId, Long languageId) {
    this.destinationId = destinationId;
    this.languageId = languageId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DestinationTranslationId that = (DestinationTranslationId) o;
    return Objects.equals(getDestinationId(), that.getDestinationId())
        && Objects.equals(getLanguageId(), that.getLanguageId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDestinationId(), getLanguageId());
  }
}
