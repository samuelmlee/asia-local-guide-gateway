package com.asialocalguide.gateway.core.domain.destination;

import lombok.Getter;

@Getter
public enum LanguageCode {
  EN,
  FR;

  public String toDbValue() {
    return this.name().toLowerCase();
  }

  public static LanguageCode from(String languageCode) {
    if (languageCode == null || languageCode.isEmpty()) {
      throw new IllegalArgumentException("Language code cannot be null or empty");
    }
    if (!"EN".equalsIgnoreCase(languageCode) && !"FR".equalsIgnoreCase(languageCode)) {
      throw new IllegalArgumentException("Language code must be either EN or FR");
    }
    return LanguageCode.valueOf(languageCode.toUpperCase());
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
