package com.asialocalguide.gateway.core.domain.planning;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

import com.asialocalguide.gateway.destination.domain.LanguageCode;

public record ProviderPlanningRequest(@NotNull LocalDate startDate, @NotNull LocalDate endDate, Integer duration,
		List<String> activityTags, @NotEmpty String providerDestinationId, LanguageCode languageCode) {
}
