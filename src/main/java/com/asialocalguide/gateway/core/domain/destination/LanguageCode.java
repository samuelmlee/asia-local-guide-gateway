package com.asialocalguide.gateway.core.domain.destination;

import java.util.Optional;

/*
 * This enum is mapped to the Language entity's code attribute in the database
 * To add new languages:
 *
 * 1. Add the two-letter ISO code in the enum
 * 2. Make sure the Language table has corresponding entries
 */
public enum LanguageCode {
  EN,
  FR;

  public static Optional<LanguageCode> from(String languageString) {

    if (languageString == null || languageString.isEmpty()) {
      return Optional.empty();
    }

    String lowerCase = languageString.toLowerCase();

    for (LanguageCode lc : values()) {
      if (lc.toString().equals(lowerCase)) {
        return Optional.of(lc);
      }
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
