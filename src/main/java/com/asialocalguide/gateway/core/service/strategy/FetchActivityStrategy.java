package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import java.util.List;
import java.util.Set;

public interface FetchActivityStrategy {

  BookingProviderName getProviderName();

  List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds);
}
