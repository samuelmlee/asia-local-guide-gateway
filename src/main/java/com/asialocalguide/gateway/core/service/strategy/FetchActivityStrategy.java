package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.CommonPersistableActivity;

import java.util.List;
import java.util.Set;

/**
 * Strategy for fetching full activity details from a specific booking provider.
 *
 * <p>Implementations are keyed by {@link BookingProviderName} and selected at runtime
 * to retrieve and convert provider-specific activity data into {@link CommonPersistableActivity} objects.
 */
public interface FetchActivityStrategy {

	/**
	 * Returns the {@link BookingProviderName} this strategy handles.
	 *
	 * @return the provider name; never {@code null}
	 */
	BookingProviderName getProviderName();

	/**
	 * Fetches activity details from the provider for the given set of activity IDs.
	 *
	 * @param activityIds the provider-assigned identifiers of activities to retrieve; must not be {@code null}
	 * @return list of persistable activity objects; never {@code null}
	 */
	List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds);
}
