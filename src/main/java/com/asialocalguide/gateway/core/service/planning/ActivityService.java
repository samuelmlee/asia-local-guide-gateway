package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.domain.planning.ActivityImage;
import com.asialocalguide.gateway.core.domain.planning.ActivityTranslation;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
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

  public Set<String> findExistingIdsByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
    return activityRepository.findExistingIdsByProviderNameAndIds(providerName, activityIds);
  }

  public Set<Activity> findActivitiesByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
    return activityRepository.findActivitiesByProviderNameAndIds(providerName, activityIds);
  }

  public List<Activity> saveAll(List<Activity> activities) {
    return activityRepository.saveAll(activities);
  }

  public void persistNewActivitiesByProvider(Map<BookingProviderName, Set<String>> providerNameToId) {

    List<Activity> activitiesToPersist =
        fetchActivityStrategies.stream()
            .flatMap(
                strategy -> {
                  BookingProviderName providerName = strategy.getProviderName();

                  Set<String> activityIds = providerNameToId.get(providerName);

                  Set<String> cachedIds = findExistingIdsByProviderNameAndIds(providerName, activityIds);
                  Set<String> newIds =
                      activityIds.stream().filter(id -> !cachedIds.contains(id)).collect(Collectors.toSet());

                  List<CommonPersistableActivity> persistableActivities;

                  try {
                    persistableActivities = strategy.fetchProviderActivities(newIds);
                  } catch (Exception e) {
                    log.error(
                        "Error during fetching of activities from Provider : {}, for ActivityIds: {}, while processing"
                            + " Provider To Id Map: {}",
                        strategy.getProviderName(),
                        newIds,
                        providerNameToId,
                        e);
                    return null;
                  }

                  Optional<BookingProvider> providerOpt = bookingProviderService.getBookingProviderByName(providerName);

                  if (providerOpt.isEmpty()) {
                    log.warn(
                        "Provider not found for providerName: {}, processing: activityIds: {}, ",
                        providerName,
                        activityIds);
                    return null;
                  }

                  return convertToActivities(persistableActivities, providerOpt.get()).stream();
                })
            .filter(Objects::nonNull)
            .toList();

    saveAll(activitiesToPersist);
  }

  private List<Activity> convertToActivities(
      List<CommonPersistableActivity> persistableActivities, BookingProvider bookingProvider) {
    return persistableActivities.stream()
        .map(persistable -> toActivity(persistable, bookingProvider))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  private Optional<Activity> toActivity(CommonPersistableActivity persistable, BookingProvider bookingProvider) {

    Activity activity =
        new Activity(
            persistable.providerId(),
            bookingProvider,
            persistable.review().averageRating(),
            persistable.review().reviewCount(),
            persistable.durationInMinutes(),
            persistable.providerUrl());

    // Set translations
    Map<LanguageCode, String> titlesByLanguage =
        persistable.title().stream()
            .collect(
                Collectors.toMap(
                    CommonPersistableActivity.Translation::languageCode, CommonPersistableActivity.Translation::value));

    Map<LanguageCode, String> descriptionsByLanguage =
        persistable.description().stream()
            .collect(
                Collectors.toMap(
                    CommonPersistableActivity.Translation::languageCode, CommonPersistableActivity.Translation::value));

    // Title is required
    titlesByLanguage.forEach(
        (languageCode, title) -> {
          String description = descriptionsByLanguage.get(languageCode);
          activity.addTranslation(new ActivityTranslation(languageCode, title, description));
        });

    // Set images
    Optional.ofNullable(persistable.images())
        .ifPresent(
            images ->
                images.forEach(
                    image ->
                        activity.addImage(
                            new ActivityImage(image.height(), image.width(), image.url(), image.type()))));

    return Optional.of(activity);
  }
}
