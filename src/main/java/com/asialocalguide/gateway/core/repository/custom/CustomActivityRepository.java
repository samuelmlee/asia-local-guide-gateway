package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.Activity;
import java.util.Set;

public interface CustomActivityRepository {

  Set<String> findExistingIdsByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds);

  Set<Activity> findActivitiesByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds);
}
