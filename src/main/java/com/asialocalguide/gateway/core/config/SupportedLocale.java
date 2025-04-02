package com.asialocalguide.gateway.core.config;

import lombok.Getter;

import java.util.stream.Stream;

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
}
