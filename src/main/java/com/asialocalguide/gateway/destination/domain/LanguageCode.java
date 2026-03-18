package com.asialocalguide.gateway.destination.domain;

import java.util.Optional;

/**
 * ISO 639-1 two-letter language codes supported by the application.
 *
 * <p>Mapped to the {@code code} column of the {@code Language} database table.
 * To add a new language:
 * <ol>
 *   <li>Add the two-letter ISO code as a new enum constant.</li>
 *   <li>Ensure the {@code Language} table has a corresponding row.</li>
 * </ol>
 */
public enum LanguageCode {
	EN, FR;

	/**
	 * Returns the {@link LanguageCode} matching the given ISO 639-1 language string,
	 * performing a case-insensitive comparison.
	 *
	 * @param languageString the language string to look up; returns empty if {@code null} or blank
	 * @return an Optional containing the matching code, or empty if no match is found
	 */
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
