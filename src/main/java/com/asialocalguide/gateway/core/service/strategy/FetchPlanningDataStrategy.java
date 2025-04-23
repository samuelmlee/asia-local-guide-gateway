package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;

public interface FetchPlanningDataStrategy {

  BookingProviderName getProviderName();

  ProviderPlanningData fetchProviderPlanningData(PlanningRequestDTO request, LanguageCode languageCode);
}
