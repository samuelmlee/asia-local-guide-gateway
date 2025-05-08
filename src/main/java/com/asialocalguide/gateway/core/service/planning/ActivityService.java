package com.asialocalguide.gateway.core.service.planning;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.domain.planning.ActivityImage;
import com.asialocalguide.gateway.core.domain.planning.ActivityTranslation;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.core.exception.ActivityCachingException;
import com.asialocalguide.gateway.core.repository.ActivityRepository;
import com.asialocalguide.gateway.core.service.LanguageService;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import com.asialocalguide.gateway.core.service.strategy.FetchActivityStrategy;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ActivityService {

  private final ActivityRepository activityRepository;

  private final BookingProviderService bookingProviderService;

  private final LanguageService languageService;

  private final List<FetchActivityStrategy> fetchActivityStrategies;

  public ActivityService(
      ActivityRepository activityRepository,
      BookingProviderService bookingProviderService,
      LanguageService languageService,
      List<FetchActivityStrategy> fetchActivityStrategies) {
    this.activityRepository = activityRepository;
    this.bookingProviderService = bookingProviderService;
    this.languageService = languageService;
    this.fetchActivityStrategies = fetchActivityStrategies;
  }

  public Set<String> findExistingIdsByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
    if (providerName == null || activityIds == null || activityIds.isEmpty()) {
      throw new IllegalArgumentException("Provider name or activity IDs cannot be null or empty");
    }
    return activityRepository.findExistingIdsByProviderNameAndIds(providerName, activityIds);
  }

  public Set<Activity> findActivitiesByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
    if (providerName == null || activityIds == null || activityIds.isEmpty()) {
      throw new IllegalArgumentException("Provider name or activity IDs cannot be null or empty");
    }

    return activityRepository.findActivitiesByProviderNameAndIds(providerName, activityIds);
  }

  public List<Activity> saveAll(List<Activity> activities) {
    if (activities == null || activities.isEmpty()) {
      throw new IllegalArgumentException("Activities cannot be null or empty");
    }

    return activityRepository.saveAll(activities);
  }

  @Transactional
  public void cacheNewActivitiesByProvider(Map<BookingProviderName, Set<String>> providerNameToId) {

    validatePersistNewActivitiesInput(providerNameToId, fetchActivityStrategies);

    log.info("Caching new activities for provider name to ID mapping: {}", providerNameToId);

    List<FetchActivityStrategy> strategiesToUse =
        fetchActivityStrategies.stream()
            .filter(strategy -> providerNameToId.containsKey(strategy.getProviderName()))
            .toList();

    if (strategiesToUse.isEmpty()) {
      throw new ActivityCachingException(
          String.format(
              "No strategies found for provider name to ID mapping: %s, strategies in service: %s",
              providerNameToId, strategiesToUse));
    }

    Map<BookingProviderName, BookingProvider> nameToProvider =
        bookingProviderService.getAllBookingProviders().stream()
            .collect(Collectors.toMap(BookingProvider::getName, Function.identity()));

    if (nameToProvider.isEmpty()) {
      throw new ActivityCachingException("No booking providers returned from BookingProviderService");
    }

    Map<LanguageCode, Language> codeToLanguage =
        languageService.getAllLanguages().stream().collect(Collectors.toMap(Language::getCode, Function.identity()));

    if (codeToLanguage.isEmpty()) {
      throw new ActivityCachingException("No Language returned from LanguageService");
    }

    List<Activity> activitiesToPersist = new ArrayList<>();

    for (FetchActivityStrategy strategy : strategiesToUse) {
      BookingProviderName providerName = strategy.getProviderName();
      BookingProvider provider = nameToProvider.get(providerName);

      if (provider == null || providerName == null) {
        log.warn("ProviderName: {} not found for strategy: {}", providerName, strategy);
        continue;
      }

      try {
        Set<String> newIds = buildActivityIdsForCaching(strategy, providerNameToId);
        if (newIds == null || newIds.isEmpty()) {
          log.info("No new activities to fetch for providerName: {}", providerName);
          continue;
        }

        List<CommonPersistableActivity> fetchedActivities = strategy.fetchProviderActivities(newIds);

        List<Activity> activities = convertToActivities(fetchedActivities, provider, codeToLanguage);
        activitiesToPersist.addAll(activities);

      } catch (Exception e) {
        log.warn("Error while fetching activities to cache for provider: " + providerName, e);
      }
    }

    if (!activitiesToPersist.isEmpty()) {
      saveAll(activitiesToPersist);
      log.info("Successfully persisted {} new activities", activitiesToPersist.size());
    } else {
      log.info("No new activities to persist");
    }
  }

  private void validatePersistNewActivitiesInput(
      Map<BookingProviderName, Set<String>> providerNameToId, List<FetchActivityStrategy> fetchActivityStrategies) {
    if (providerNameToId == null
        || providerNameToId.isEmpty()
        || fetchActivityStrategies == null
        || fetchActivityStrategies.isEmpty()) {
      throw new ActivityCachingException(
          "Provider name to ID mapping or fetch activity strategies in ActivityService are null or empty");
    }
  }

  private Set<String> buildActivityIdsForCaching(
      FetchActivityStrategy strategy, Map<BookingProviderName, Set<String>> providerNameToId) {

    if (strategy == null || providerNameToId == null || providerNameToId.isEmpty()) {
      log.warn("Strategy or providerNameToId is invalid in buildNewActivityIds");
      return Set.of();
    }

    BookingProviderName providerName = strategy.getProviderName();

    Set<String> activityIds = providerNameToId.get(providerName);

    if (activityIds == null || activityIds.isEmpty()) {
      log.warn("No activity IDs found for providerName: {} in buildActivityIdsForCaching", providerName);
      return Set.of();
    }

    try {
      Set<String> cachedIds = findExistingIdsByProviderNameAndIds(providerName, activityIds);
      if (cachedIds == null || cachedIds.isEmpty()) {
        log.info("No existing activity IDs found for providerName: {} and activityIds:  {}", providerName, activityIds);
        return activityIds;
      }

      return activityIds.stream()
          .filter(id -> id != null && !id.isBlank() && !cachedIds.contains(id))
          .collect(Collectors.toSet());

    } catch (Exception ex) {
      log.warn(
          "Error while building activity IDs for caching for providerName: {}, activityIds: {}",
          providerName,
          activityIds,
          ex);

      return Set.of();
    }
  }

  private List<Activity> convertToActivities(
      List<CommonPersistableActivity> persistableActivities,
      BookingProvider bookingProvider,
      Map<LanguageCode, Language> codeToLanguage) {

    if (persistableActivities == null || persistableActivities.isEmpty() || bookingProvider == null) {
      log.warn(
          "Invalid persistableActivities; {} or BookingProvider: {} in convertToActivities",
          persistableActivities,
          bookingProvider);
      return List.of();
    }

    return persistableActivities.stream()
        .map(persistable -> toActivity(persistable, bookingProvider, codeToLanguage))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<Activity> toActivity(
      CommonPersistableActivity persistable,
      BookingProvider bookingProvider,
      Map<LanguageCode, Language> codeToLanguage) {

    if (persistable == null || bookingProvider == null) {
      log.warn("Invalid input in toActivity, persistable: {}, bookingProvider: {}", persistable, bookingProvider);
      return Optional.empty();
    }

    try {

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
              .filter(title -> title.languageCode() != null)
              .collect(
                  Collectors.toMap(
                      CommonPersistableActivity.Translation::languageCode,
                      CommonPersistableActivity.Translation::value));

      Map<LanguageCode, String> descriptionsByLanguage =
          persistable.description().stream()
              .filter(translation -> translation.languageCode() != null)
              .collect(
                  Collectors.toMap(
                      CommonPersistableActivity.Translation::languageCode,
                      CommonPersistableActivity.Translation::value));

      // Title is required
      titlesByLanguage.forEach(
          (languageCode, title) -> {
            try {

              String description = descriptionsByLanguage.get(languageCode);
              Language language = codeToLanguage.get(languageCode);

              if (language == null) {
                log.warn("Language not found for code: {}", languageCode);
                return;
              }

              activity.addTranslation(new ActivityTranslation(activity, language, title, description));

            } catch (Exception ex) {
              log.warn(
                  "Error while adding translation for activity: {}, languageCode: {}, title: {}, description: {}",
                  activity,
                  languageCode,
                  title,
                  descriptionsByLanguage.get(languageCode),
                  ex);
            }
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

    } catch (Exception e) {
      log.warn(
          "Error while converting a CommonPersistableActivity : {}, for BookingProvider: {} in toActivity",
          persistable,
          bookingProvider,
          e);

      return Optional.empty();
    }
  }
}
