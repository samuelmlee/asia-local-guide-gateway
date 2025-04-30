package com.asialocalguide.gateway.viator.service;

import static java.util.Objects.requireNonNull;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.*;
import com.asialocalguide.gateway.core.service.composer.ActivityProvider;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorActivityServiceException;
import com.asialocalguide.gateway.viator.util.ViatorActivityAdapter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ViatorActivityService implements ActivityProvider {

  public static final BookingProviderName providerName = BookingProviderName.VIATOR;

  private static final String DEFAULT_CURRENCY = "EUR";
  private static final int MIN_RATING = 4;
  private static final int MAX_RATING = 5;

  private final ViatorClient viatorClient;

  public ViatorActivityService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  @Override
  public BookingProviderName getProviderName() {
    return providerName;
  }

  @Override
  public ProviderPlanningData fetchProviderPlanningData(ProviderPlanningRequest request) {
    validatePlanningRequest(request);

    try {
      ViatorActivitySearchDTO searchDTO = buildActivitySearchDTO(request);
      Map<String, ViatorActivityDTO> idToActivities = fetchValidActivities(request.languageCode(), searchDTO);
      List<ViatorActivityAvailabilityDTO> availabilities = fetchActivityAvailabilities(idToActivities.values());

      // Filter out activities with no availability data
      List<ViatorActivityDTO> activitiesToProcess = filterNoDataActivities(idToActivities, availabilities);

      List<CommonActivity> commonActivities =
          activitiesToProcess.stream().map(ViatorActivityAdapter::toCommon).toList();

      return new ProviderPlanningData(
          commonActivities, mapToActivityData(activitiesToProcess, availabilities, request), request.startDate());
    } catch (Exception e) {
      throw new ViatorActivityServiceException("Failed to fetch activity data", e);
    }
  }

  @Override
  public List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds) {
    if (activityIds == null || activityIds.isEmpty()) {
      throw new IllegalArgumentException("Activity IDs to fetch Viator Activities cannot be null or empty");
    }
    log.info("Fetching Viator activities for languages: {}", Arrays.toString(LanguageCode.values()));

    Map<LanguageCode, Map<String, ViatorActivityDetailDTO>> languageToActivities =
        fetchLanguageToActivities(activityIds);

    // Use English language activities as base for creating CommonPersistableActivity, other
    // languages used for translations
    Map<String, ViatorActivityDetailDTO> idToActivitiesEnDTOs = languageToActivities.get(LanguageCode.EN);

    if (idToActivitiesEnDTOs == null || idToActivitiesEnDTOs.isEmpty()) {
      throw new ViatorActivityServiceException("Failed to process any activities for English language");
    }

    return idToActivitiesEnDTOs.values().stream()
        .map(dto -> createCommonPersistableActivity(dto, languageToActivities))
        .filter(Objects::nonNull)
        .toList();
  }

  private void validatePlanningRequest(ProviderPlanningRequest request) {
    Objects.requireNonNull(request);

    if (request.startDate().isAfter(request.endDate())) {
      throw new IllegalArgumentException("Start date must be before end date");
    }

    if (!NumberUtils.isParsable(request.providerDestinationId())) {
      throw new IllegalArgumentException("Invalid Viator destination ID format");
    }
  }

  private ViatorActivitySearchDTO buildActivitySearchDTO(ProviderPlanningRequest request) {
    List<Integer> activityTagIds = convertActivityTags(request.activityTags());

    return new ViatorActivitySearchDTO(
        new ViatorActivitySearchDTO.Filtering(
            Long.parseLong(request.providerDestinationId()),
            activityTagIds,
            request.startDate(),
            request.endDate(),
            new ViatorActivitySearchDTO.Range(MIN_RATING, MAX_RATING)),
        new ViatorActivitySearchDTO.Sorting(
            ViatorActivitySortingType.TRAVELER_RATING, ViatorActivitySortingOrder.DESCENDING),
        createPagination(request),
        DEFAULT_CURRENCY);
  }

  private List<Integer> convertActivityTags(List<String> tags) {
    return Optional.ofNullable(tags).orElse(Collections.emptyList()).stream()
        .map(this::parseActivityTag)
        .filter(Objects::nonNull)
        .toList();
  }

  private Integer parseActivityTag(String tag) {
    try {
      return Integer.valueOf(tag);
    } catch (NumberFormatException e) {
      log.warn("Invalid activity tag format: {}", tag);
      return null;
    }
  }

  private ViatorActivitySearchDTO.Pagination createPagination(ProviderPlanningRequest request) {
    int durationDays = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate());
    // Fetch 4 activities per day
    int itemsPerPage = Math.max(durationDays, 1) * 4;
    return new ViatorActivitySearchDTO.Pagination(1, itemsPerPage);
  }

  private Map<String, ViatorActivityDTO> fetchValidActivities(
      LanguageCode languageCode, ViatorActivitySearchDTO searchDTO) {
    return viatorClient
        .getActivitiesByRequestAndLanguage(
            requireNonNull(languageCode, "Locale must not be null").toString(),
            requireNonNull(searchDTO, "SearchDTO must not be null"))
        .stream()
        // Activities without duration should not be processed
        .filter(dto -> dto.getDurationMinutes() > 0)
        .collect(Collectors.toMap(ViatorActivityDTO::productCode, Function.identity()));
  }

  private List<ViatorActivityAvailabilityDTO> fetchActivityAvailabilities(Collection<ViatorActivityDTO> activities) {
    if (activities == null || activities.isEmpty()) {
      return List.of();
    }

    List<ViatorActivityAvailabilityDTO> result = new CopyOnWriteArrayList<>();
    List<Future<Void>> futures = new ArrayList<>();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      // Submit a task for each activity
      for (ViatorActivityDTO activity : activities) {
        if (activity == null || activity.productCode() == null) {
          log.warn("Skipping null ViatorActivityDTO in fetchActivityAvailabilities.");
          continue;
        }
        futures.add(
            executor.submit(
                () -> {
                  try {
                    Optional<ViatorActivityAvailabilityDTO> availabilityOpt =
                        viatorClient.getAvailabilityByProductCode(activity.productCode());

                    availabilityOpt.ifPresent(result::add);
                  } catch (Exception ex) {
                    log.warn("Error while fetching Activity Availability for product code: {}", activity.productCode());
                  }
                  return null;
                }));
      }

      // Wait for all futures to complete
      waitForTaskCompletion(futures);

      return result;
    }
  }

  private List<ViatorActivityDTO> filterNoDataActivities(
      Map<String, ViatorActivityDTO> idToActivities, List<ViatorActivityAvailabilityDTO> availabilities) {

    return availabilities.stream()
        .map(availability -> idToActivities.get(availability.productCode()))
        .filter(Objects::nonNull)
        .toList();
  }

  private ActivityPlanningData mapToActivityData(
      List<ViatorActivityDTO> activities,
      List<ViatorActivityAvailabilityDTO> availabilities,
      ProviderPlanningRequest request) {
    try {
      return ViatorActivityAvailabilityMapper.mapToActivityData(
          activities, availabilities, request.startDate(), request.endDate());
    } catch (Exception e) {
      log.error("Failed to map activity data: {}", e.getMessage());
      throw new ViatorActivityServiceException("Activity data mapping failed", e);
    }
  }

  private Map<LanguageCode, Map<String, ViatorActivityDetailDTO>> fetchLanguageToActivities(Set<String> activityIds) {

    Map<LanguageCode, Map<String, ViatorActivityDetailDTO>> result =
        new ConcurrentHashMap<>(LanguageCode.values().length);

    // Initialize maps for each language
    for (LanguageCode language : LanguageCode.values()) {
      result.put(language, new ConcurrentHashMap<>());
    }

    List<Future<Void>> futures = new ArrayList<>();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      // Submit tasks for each language and activity ID
      for (LanguageCode language : LanguageCode.values()) {
        for (String id : activityIds) {
          futures.add(
              executor.submit(
                  () -> {
                    try {
                      Optional<ViatorActivityDetailDTO> activityOpt =
                          viatorClient.getActivityByIdAndLanguage(language.toString(), id);

                      activityOpt.ifPresent(activity -> result.get(language).put(activity.productCode(), activity));

                    } catch (Exception ex) {
                      log.warn(
                          "Failed to fetch Viator activity for id {} for language {} : {}",
                          id,
                          language,
                          ex.getMessage());
                    }
                    return null;
                  }));
        }
      }

      waitForTaskCompletion(futures);

      return result;
    }
  }

  private CommonPersistableActivity createCommonPersistableActivity(
      ViatorActivityDetailDTO dto, Map<LanguageCode, Map<String, ViatorActivityDetailDTO>> languageToActivities) {

    if (dto == null) {
      log.warn("Skipping null ViatorActivityDetailDTO in createCommonPersistableActivity.");
      return null;
    }

    return new CommonPersistableActivity(
        resolveTranslations(dto.productCode(), languageToActivities, ViatorActivityDetailDTO::title),
        resolveTranslations(dto.productCode(), languageToActivities, ViatorActivityDetailDTO::description),
        mapActivityDetailImages(dto),
        mapActivityDetailReview(dto),
        dto.getDurationMinutes(),
        dto.productUrl(),
        BookingProviderName.VIATOR,
        dto.productCode());
  }

  private List<CommonPersistableActivity.Image> mapActivityDetailImages(ViatorActivityDetailDTO dto) {
    List<CommonPersistableActivity.Image> images = new ArrayList<>();

    dto.getCoverImage(variant -> variant.width() == 480 && variant.height() == 320)
        .ifPresent(
            variant ->
                images.add(
                    new CommonPersistableActivity.Image(
                        ImageType.MOBILE, variant.height(), variant.width(), variant.url())));

    dto.getCoverImage(variant -> variant.width() == 720 && variant.height() == 480)
        .ifPresent(
            variant ->
                images.add(
                    new CommonPersistableActivity.Image(
                        ImageType.DESKTOP, variant.height(), variant.width(), variant.url())));

    return images;
  }

  private CommonPersistableActivity.Review mapActivityDetailReview(ViatorActivityDetailDTO dto) {

    double averageRating = dto.reviews().combinedAverageRating();
    int reviewCount = dto.reviews().totalReviews();

    return new CommonPersistableActivity.Review(averageRating, reviewCount);
  }

  private List<CommonPersistableActivity.Translation> resolveTranslations(
      String productCode,
      Map<LanguageCode, Map<String, ViatorActivityDetailDTO>> languageToActivities,
      Function<ViatorActivityDetailDTO, String> mapper) {

    return languageToActivities.entrySet().stream()
        .map(
            entry -> {
              Map<String, ViatorActivityDetailDTO> idToActivities = entry.getValue();

              if (idToActivities == null) {
                log.debug(
                    "No idToActivities Map found for language: {} while processing productCode: {}",
                    entry.getKey(),
                    productCode);
                return null;
              }

              ViatorActivityDetailDTO dto = idToActivities.get(productCode);
              if (dto == null) {
                log.debug("No translation found for productCode: {} in language: {}", productCode, entry.getKey());
                return null;
              }
              return new CommonPersistableActivity.Translation(entry.getKey(), mapper.apply(dto));
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private void waitForTaskCompletion(List<Future<Void>> futures) {
    for (Future<Void> future : futures) {
      try {
        future.get();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Task was interrupted: {}", e.getMessage());
        break;
      } catch (ExecutionException e) {
        log.warn("Error during task execution: {}", e.getMessage());
      } catch (Exception e) {
        log.warn("Error waiting for task completion: {}", e.getMessage());
      }
    }
  }
}
