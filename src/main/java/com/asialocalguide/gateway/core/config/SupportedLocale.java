package com.asialocalguide.gateway.core.config;

import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enumerates the locales that the application supports for content localisation.
 *
 * <p>The enum values drive locale negotiation in {@link AcceptHeaderResolver} and
 * determine which language codes are accepted for translation queries.
 */
@Getter
public enum SupportedLocale {
	ENGLISH("en", true), FRENCH("fr", false);

	private final String code;

	private final boolean isDefault;

	SupportedLocale(String code, boolean isDefault) {
		this.code = code;
		this.isDefault = isDefault;
	}

	/**
	 * Returns a sequential stream over all supported locales.
	 *
	 * @return stream of {@link SupportedLocale} values
	 */
	public static Stream<SupportedLocale> stream() {
		return Stream.of(SupportedLocale.values());
	}
}
