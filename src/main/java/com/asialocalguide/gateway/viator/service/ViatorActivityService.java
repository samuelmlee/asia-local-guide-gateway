package com.asialocalguide.gateway.viator.service;

import static java.util.Objects.requireNonNull;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.*;
import com.asialocalguide.gateway.core.service.composer.ActivityProvider;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorActivityServiceException;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import com.asialocalguide.gateway.viator.util.ViatorActivityAdapter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    Map<LanguageCode, List<CompletableFuture<Optional<ViatorActivityDetailDTO>>>> futuresByLanguage =
        new EnumMap<>(LanguageCode.class);

    for (LanguageCode language : LanguageCode.values()) {
      List<CompletableFuture<Optional<ViatorActivityDetailDTO>>> languageFutures =
          activityIds.stream()
              .map(
                  id ->
                      CompletableFuture.supplyAsync(
                              () -> viatorClient.getActivityByIdAndLanguage(language.toString(), id))
                          .exceptionally(
                              ex -> {
                                log.warn(
                                    "Failed to fetch Viator activity {} for language {}: {}",
                                    id,
                                    language,
                                    ex.getMessage());
                                return Optional.empty();
                              }))
              .toList();

      futuresByLanguage.put(language, languageFutures);
    }

    // Process results by language
    Map<LanguageCode, Map<String, ViatorActivityDetailDTO>> languageToActivities = new EnumMap<>(LanguageCode.class);

    futuresByLanguage.forEach(
        (language, futures) -> {
          try {
            // Wait for all futures of the language to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Process results
            Map<String, ViatorActivityDetailDTO> activityMap =
                futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toMap(ViatorActivityDetailDTO::productCode, Function.identity()));

            if (!activityMap.isEmpty()) {
              languageToActivities.put(language, activityMap);
            } else {
              log.warn("No activities found for language: {}", language);
            }
          } catch (Exception e) {
            log.error("Error processing activities for language {}: {}", language, e.getMessage());
            throw new ViatorApiException(String.format("Failed to process activities for language: %s", language), e);
          }
        });

    // Use English destinations as base for creating CommonPersistableActivity, other languages used
    // for translations
    Map<String, ViatorActivityDetailDTO> idToActivitiesEnDTOs = languageToActivities.get(LanguageCode.EN);

    return idToActivitiesEnDTOs.values().stream()
        .map(dto -> createCommonPersistableActivity(dto, languageToActivities))
        .filter(Objects::nonNull)
        .toList();
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
        List.of(),
        new CommonPersistableActivity.Review(5.0, 1),
        dto.getDurationMinutes(),
        new CommonPersistableActivity.Pricing(0.0, DEFAULT_CURRENCY),
        dto.productUrl(),
        List.of(),
        BookingProviderName.VIATOR,
        dto.productCode());
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

    List<CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> futures = createAvailabilityFutures(activities);

    var futureArray = futures.toArray(new CompletableFuture[0]);

    return CompletableFuture.allOf(futureArray)
        .thenApply(
            ignored ->
                futures.stream().map(CompletableFuture::join).filter(Optional::isPresent).map(Optional::get).toList())
        .join();
  }

  private List<CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> createAvailabilityFutures(
      Collection<ViatorActivityDTO> activities) {

    return activities.stream()
        .map(
            activity -> {
              String productCode = requireNonNull(activity.productCode());

              return CompletableFuture.supplyAsync(() -> viatorClient.getAvailabilityByProductCode(productCode))
                  .exceptionally(
                      ex -> {
                        log.warn("Error while fetching Activity Availability for product code : {}", productCode);
                        return Optional.empty();
                      });
            })
        .toList();
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
}
