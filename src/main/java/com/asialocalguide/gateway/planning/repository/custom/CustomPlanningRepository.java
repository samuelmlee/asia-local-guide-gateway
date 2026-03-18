package com.asialocalguide.gateway.planning.repository.custom;

import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.planning.domain.Planning;

import java.util.List;
import java.util.UUID;

/**
 * Custom repository interface for {@link Planning} QueryDSL-based lookups.
 */
public interface CustomPlanningRepository {

	/**
	 * Returns {@code true} if the given user already has a planning with the specified name.
	 *
	 * @param appUserId the user's UUID
	 * @param name      the planning name to check
	 * @return {@code true} if a duplicate exists, {@code false} otherwise
	 */
	boolean existsByAppUserIdAndName(UUID appUserId, String name);

	/**
	 * Returns all plannings for the given user, with day plans, activities, and translations
	 * for the specified language eagerly fetched.
	 *
	 * @param appUserId    the user's UUID
	 * @param languageCode the language to filter translations by
	 * @return list of plannings; never {@code null}
	 */
	List<Planning> getPlanningsByAppUserIdAndLanguageCode(UUID appUserId, LanguageCode languageCode);
}
