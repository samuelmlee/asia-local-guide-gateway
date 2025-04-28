package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.domain.planning.ActivityId;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.core.repository.ActivityRepository;
import com.asialocalguide.gateway.core.service.strategy.FetchActivityStrategy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityService {

  private final ActivityRepository activityRepository;

  private final List<FetchActivityStrategy> fetchActivityStrategies;

  public ActivityService(ActivityRepository activityRepository, List<FetchActivityStrategy> fetchActivityStrategies) {
    this.activityRepository = activityRepository;
    this.fetchActivityStrategies = fetchActivityStrategies;
  }

  public Set<String> findExistingActivityIdsByProviderName(BookingProviderName providerName, Set<String> activityIds) {
    return activityRepository.findExistingActivityIdsByProviderName(providerName, activityIds);
  }

  public List<Activity> saveAll(List<Activity> activities) {
    return activityRepository.saveAll(activities);
  }

  public List<Activity> persistNewActivitiesByProvider(Map<BookingProviderName, Set<String>> providerNameToId) {

    List<CommonPersistableActivity> activitiesToPersist =
        fetchActivityStrategies.stream()
            .flatMap(
                strategy -> {
                  BookingProviderName providerName = strategy.getProviderName();

                  Set<String> activityIds = providerNameToId.get(providerName);

                  Set<String> cachedIds = findExistingActivityIdsByProviderName(providerName, activityIds);
                  Set<String> newIds =
                      activityIds.stream().filter(id -> !cachedIds.contains(id)).collect(Collectors.toSet());

                  try {
                    return strategy.fetchProviderActivities(newIds).stream();
                  } catch (Exception e) {
                    log.error("Error during fetching of activities from Provider : {}", strategy.getProviderName(), e);
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .toList();

    return saveAll(convertToActivities(activitiesToPersist));
  }

  private List<Activity> convertToActivities(List<CommonPersistableActivity> persistableActivities) {
    return persistableActivities.stream().map(this::toActivity).toList();
  }

  private Activity toActivity(CommonPersistableActivity persistable) {
    Activity activity = new Activity();

    // Set composite ID
    ActivityId activityId = new ActivityId();

    return activity;
  }
}
