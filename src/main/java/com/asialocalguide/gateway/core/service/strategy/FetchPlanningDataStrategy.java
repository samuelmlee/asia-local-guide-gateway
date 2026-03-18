package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningData;
import com.asialocalguide.gateway.planning.dto.PlanningRequestDTO;

/**
 * Strategy for fetching planning data (available time slots and activity IDs) from a specific
 * booking provider.
 *
 * <p>Implementations are selected by {@link BookingProviderName} and translate a
 * {@link PlanningRequestDTO} into provider-specific API calls.
 */
public interface FetchPlanningDataStrategy {

	/**
	 * Returns the {@link BookingProviderName} this strategy handles.
	 *
	 * @return the provider name; never {@code null}
	 */
	BookingProviderName getProviderName();

	/**
	 * Fetches planning data for the given request and language from the provider.
	 *
	 * @param request      the planning request containing destination, dates, and activity tag filters
	 * @param languageCode the language in which to return activity content
	 * @return provider planning data including available activities and time slots
	 */
	ProviderPlanningData fetchProviderPlanningData(PlanningRequestDTO request, LanguageCode languageCode);
}
