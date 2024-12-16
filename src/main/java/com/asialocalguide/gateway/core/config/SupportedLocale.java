package com.asialocalguide.gateway.core.config;

import lombok.Getter;

@Getter
public enum SupportedLocale {
  ENGLISH("en"),
  FRENCH("fr"),
  ;

  private final String code;

  SupportedLocale(String code) {
    this.code = code;
  }
}
