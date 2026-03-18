package com.asialocalguide.gateway.destination.domain;

import java.util.Optional;

/**
 * Implemented by entities that carry localized name translations (e.g. {@code Country},
 * {@code Destination}).
 *
 * <p>Provides a uniform method for retrieving the name in a requested language without
 * exposing the underlying translation collection.
 */
public interface Translatable {

	/**
	 * Returns the translated name for the given language code, if available.
	 *
	 * <p>Implementations typically require translation associations to be eagerly loaded
	 * before this method is called to avoid lazy-loading exceptions.
	 *
	 * @param languageCode the language to retrieve; returns empty if {@code null}
	 * @return an Optional containing the localized name, or empty if no translation exists
	 */
	Optional<String> getTranslation(LanguageCode languageCode);
}
