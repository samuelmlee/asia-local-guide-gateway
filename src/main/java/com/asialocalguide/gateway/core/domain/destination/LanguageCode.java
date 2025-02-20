package com.asialocalguide.gateway.core.domain.destination;

public enum LanguageCode {
  EN,
  FR;

  public String toDbValue() {
    return this.name().toLowerCase();
  }

  public static LanguageCode from(String code) {
    if (code == null || code.isEmpty()) {
      throw new IllegalArgumentException("Language code cannot be null or empty");
    }
    if (!"EN".equalsIgnoreCase(code) && !"FR".equalsIgnoreCase(code)) {
      throw new IllegalArgumentException("Language code must be either EN or FR");
    }
    return LanguageCode.valueOf(code.toUpperCase());
  }
}
