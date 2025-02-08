package com.asialocalguide.gateway.core.domain.destination;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum LanguageCode {
  EN("en"),
  FR("fr");

  private final String code;

  LanguageCode(String code) {
    this.code = code;
  }

  public static Optional<LanguageCode> fromLocale(Locale locale) {
    return Arrays.stream(LanguageCode.values())
        .filter(languageCode -> languageCode.getCode().equals(locale.getLanguage()))
        .findFirst();
  }
}
