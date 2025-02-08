package com.asialocalguide.gateway.core.domain.destination;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DestinationTranslationId implements Serializable {
  private Long destination;

  private String languageCode;
}
