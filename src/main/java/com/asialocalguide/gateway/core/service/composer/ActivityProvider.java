package com.asialocalguide.gateway.core.service.composer;

import java.util.List;
import java.util.Set;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.CommonPersistableActivity;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningData;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningRequest;

/**
 * Strategy interface for booking provider integrations that supply activity planning data
 * and persistable activity details.
 *
 * <p>Each provider (e.g. Viator) implements this interface so the planning pipeline can
 * fetch data from multiple providers through a uniform API.
 */
public interface ActivityProvider {

	/**
	 * Returns the {@link BookingProviderName} this implementation targets.
	 *
	 * @return the provider name; never {@code null}
	 */
	BookingProviderName getProviderName();

	/**
	 * Fetches provider-specific planning data for the given request.
	 *
	 * @param request the planning request containing destination, dates, and activity tag filters
	 * @return provider planning data including available time slots and activity IDs
	 */
	ProviderPlanningData fetchProviderPlanningData(ProviderPlanningRequest request);

	/**
	 * Fetches full activity details from the provider for the given set of activity IDs.
	 *
	 * @param activityIds the provider-assigned activity identifiers to fetch; must not be {@code null}
	 * @return list of persistable activity objects; never {@code null}
	 */
	List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds);
}
