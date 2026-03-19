package com.asialocalguide.gateway.planning.repository.custom;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.Activity;

import java.util.Set;

/**
 * Custom repository interface for {@link Activity} QueryDSL-based lookups.
 */
public interface CustomActivityRepository {

	/**
	 * Returns the subset of the given provider activity IDs that already exist in the database.
	 *
	 * @param providerName the booking provider to filter by
	 * @param activityIds  provider-specific activity IDs to check
	 * @return set of IDs that are already persisted; never {@code null}
	 */
	Set<String> findExistingIdsByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds);

	/**
	 * Returns all {@link Activity} entities matching the given provider name and IDs.
	 *
	 * @param providerName the booking provider to filter by
	 * @param activityIds  provider-specific activity IDs to retrieve
	 * @return set of matching activities; never {@code null}
	 */
	Set<Activity> findActivitiesByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds);
}
