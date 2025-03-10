package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.config.SupportedLocale;

import java.time.LocalDate;
import java.util.List;

public record ProviderPlanningRequest(
    LocalDate startDate,
    LocalDate endDate,
    Integer duration,
    List<String> activityTags,
    String providerDestinationId,
    SupportedLocale locale) {}
