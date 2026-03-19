package com.asialocalguide.gateway.planning.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

import com.asialocalguide.gateway.destination.domain.LanguageCode;

/**
 * Provider-specific planning query sent to a booking provider's API.
 *
 * @param startDate               first day of the planning period; must not be {@code null}
 * @param endDate                 last day of the planning period; must not be {@code null}
 * @param duration                number of days in the planning period
 * @param activityTags            optional list of activity tag filters
 * @param providerDestinationId   the provider's internal destination identifier; must not be empty
 * @param languageCode            requested language for localized results
 */
public record ProviderPlanningRequest(@NotNull LocalDate startDate, @NotNull LocalDate endDate, Integer duration,
		List<String> activityTags, @NotEmpty String providerDestinationId, LanguageCode languageCode) {
}
