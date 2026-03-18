package com.asialocalguide.gateway.core.service.composer;

import java.util.List;
import java.util.Set;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.CommonPersistableActivity;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningData;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningRequest;

public interface ActivityProvider {

	BookingProviderName getProviderName();

	ProviderPlanningData fetchProviderPlanningData(ProviderPlanningRequest request);

	List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds);
}
