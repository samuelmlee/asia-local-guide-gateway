package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

public interface FetchPlanningDataStrategy {

	BookingProviderName getProviderName();

	ProviderPlanningData fetchProviderPlanningData(PlanningRequestDTO request, LanguageCode languageCode);
}
