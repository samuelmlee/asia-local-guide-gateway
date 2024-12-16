package com.asialocalguide.gateway.core.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DestinationTranslationId implements Serializable {
  private Long id;

  private String locale;
}
