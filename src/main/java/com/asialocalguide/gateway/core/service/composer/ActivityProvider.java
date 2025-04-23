package com.asialocalguide.gateway.core.service.composer;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;

public interface ActivityProvider {

  BookingProviderName getProviderName();

  ProviderPlanningData fetchProviderPlanningData(ProviderPlanningRequest request);
}
