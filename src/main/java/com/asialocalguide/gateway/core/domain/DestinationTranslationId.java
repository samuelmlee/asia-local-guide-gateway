package com.asialocalguide.gateway.core.domain;

import java.io.Serializable;
import java.util.Objects;

public class DestinationTranslationId implements Serializable {
  private final String id;

  private final String locale;

  public DestinationTranslationId(String id, String locale) {
    this.id = id;
    this.locale = locale;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    DestinationTranslationId that = (DestinationTranslationId) o;
    return Objects.equals(id, that.id) && Objects.equals(locale, that.locale);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(id);
    result = 31 * result + Objects.hashCode(locale);
    return result;
  }
}
