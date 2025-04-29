package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.*;
import com.asialocalguide.gateway.core.repository.ActivityRepository;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import com.asialocalguide.gateway.core.service.strategy.FetchActivityStrategy;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityService {

  private final ActivityRepository activityRepository;

  private final BookingProviderService bookingProviderService;

  private final List<FetchActivityStrategy> fetchActivityStrategies;

  public ActivityService(
      ActivityRepository activityRepository,
      BookingProviderService bookingProviderService,
      List<FetchActivityStrategy> fetchActivityStrategies) {
    this.activityRepository = activityRepository;
    this.bookingProviderService = bookingProviderService;
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
    return persistableActivities.stream().map(this::toActivity).filter(Optional::isPresent).map(Optional::get).toList();
  }

  private Optional<Activity> toActivity(CommonPersistableActivity persistable) {

    // Set composite ID
    ActivityId activityId = new ActivityId(persistable.providerId());
    Optional<BookingProvider> providerOpt = bookingProviderService.getBookingProviderByName(persistable.providerName());

    if (providerOpt.isEmpty()) {
      log.warn(
          "Provider not found for activity: {}, providerName: {}",
          persistable.providerId(),
          persistable.providerName());
      return Optional.empty();
    }

    //    Activity activity = new Activity(
    //              activityId,
    //            providerOpt.get(),
    //              List< ActivityTranslation > translations,
    //              Double averageRating,
    //              Integer reviewCount,
    //              Integer durationMinutes,
    //              List< ActivityImage > coverImages,
    //              String bookingUrl);

    return Optional.empty();
  }
}
