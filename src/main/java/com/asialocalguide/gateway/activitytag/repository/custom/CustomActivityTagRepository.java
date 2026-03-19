package com.asialocalguide.gateway.activitytag.repository.custom;

import java.util.List;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

/**
 * Custom repository interface for {@link ActivityTag} queries that require
 * eager loading of translations and their associated language entities.
 */
public interface CustomActivityTagRepository {

	/**
	 * Returns all activity tags with their translations eagerly loaded for the given language.
	 *
	 * @param languageCode the language to filter translations by
	 * @return list of activity tags that have a translation for the specified language
	 */
	List<ActivityTag> findAllWithTranslations(LanguageCode languageCode);
}
