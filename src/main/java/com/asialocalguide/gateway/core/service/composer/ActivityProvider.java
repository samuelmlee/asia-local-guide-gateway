package com.asialocalguide.gateway.core.service.composer;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;

public interface ActivityProvider {

  BookingProviderName getProviderName();

  ProviderActivityPlanningData fetchProviderActivityData(ProviderPlanningRequest request);
}
