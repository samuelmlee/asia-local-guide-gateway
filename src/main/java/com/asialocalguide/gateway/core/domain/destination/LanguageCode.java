package com.asialocalguide.gateway.core.domain.destination;

import java.util.Optional;

public enum LanguageCode {
    EN,
    FR;

    public String toDbValue() {
        return this.name().toLowerCase();
    }

    public static Optional<LanguageCode> from(String code) {
        if (code == null || code.isEmpty()) {
            return Optional.empty();
        }
        if (!"EN".equalsIgnoreCase(code) && !"FR".equalsIgnoreCase(code)) {
            return Optional.empty();
        }
        return Optional.of(LanguageCode.valueOf(code.toUpperCase()));
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
