package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ProviderPlanningRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        Integer duration,
        List<String> activityTags,
        @NotEmpty String providerDestinationId,
        LanguageCode languageCode) {
}
