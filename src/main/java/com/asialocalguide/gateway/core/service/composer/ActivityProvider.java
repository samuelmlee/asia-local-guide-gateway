package com.asialocalguide.gateway.core.service.composer;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.CommonPersistableActivity;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningData;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningRequest;

import java.util.List;
import java.util.Set;

public interface ActivityProvider {

	BookingProviderName getProviderName();

	ProviderPlanningData fetchProviderPlanningData(ProviderPlanningRequest request);

	List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds);
}
