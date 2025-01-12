package com.asialocalguide.gateway.core.config;

import com.asialocalguide.gateway.viator.exception.ViatorDestinationMappingException;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum SupportedLocale {
  ENGLISH("en", true),
  FRENCH("fr", false);

  private final String code;

  private final boolean isDefault;

  SupportedLocale(String code, boolean isDefault) {
    this.code = code;
    this.isDefault = isDefault;
  }

  public static Stream<SupportedLocale> stream() {
    return Stream.of(SupportedLocale.values());
  }

  public static SupportedLocale getDefaultLocale() {
    return SupportedLocale.stream()
        .filter(SupportedLocale::isDefault)
        .findFirst()
        .orElseThrow(() -> new ViatorDestinationMappingException("No default locale found"));
  }
}
