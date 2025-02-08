package com.asialocalguide.gateway.core.domain.destination;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CountryTranslationId implements Serializable {
  private Long country;

  private String languageCode;
}
