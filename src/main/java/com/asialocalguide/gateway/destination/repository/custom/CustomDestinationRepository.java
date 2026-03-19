package com.asialocalguide.gateway.destination.repository.custom;

import java.util.List;

import com.asialocalguide.gateway.destination.domain.Destination;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

/**
 * Custom repository interface for {@link Destination} queries that require
 * eager loading of translations and related entities.
 */
public interface CustomDestinationRepository {

	/**
	 * Returns city and region destinations whose localized name contains the given search string,
	 * with translations and country data eagerly loaded.
	 *
	 * @param languageCode the language to filter and load translations for
	 * @param name         the substring to search for within destination names (case-insensitive)
	 * @return list of matching destinations with translations eagerly loaded; never {@code null}
	 */
	List<Destination> findCityOrRegionByNameWithEagerTranslations(LanguageCode languageCode, String name);
}
