package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningData;
import com.asialocalguide.gateway.planning.dto.PlanningRequestDTO;

public interface FetchPlanningDataStrategy {

	BookingProviderName getProviderName();

	ProviderPlanningData fetchProviderPlanningData(PlanningRequestDTO request, LanguageCode languageCode);
}
