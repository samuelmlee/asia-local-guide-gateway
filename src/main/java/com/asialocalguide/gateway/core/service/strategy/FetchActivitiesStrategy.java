package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;

public interface FetchActivitiesStrategy {

    BookingProviderName getProviderName();

    ProviderActivityData fetchProviderActivity(PlanningRequestDTO request, SupportedLocale locale);
}
