package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ViatorActivityService {

  private final ViatorClient viatorClient;

  public ViatorActivityService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public ProviderActivityData fetchProviderActivityData(ProviderPlanningRequest request) {

    ViatorActivitySearchDTO searchDTO = buildActivitySearchDTO(request);

    List<ViatorActivityDTO> activities = getViatorActivityDTOS(request.locale(), searchDTO);

    List<ViatorActivityAvailabilityDTO> availabilities = getViatorAvailabilityDTOS(activities);

    LocalDate startDate = request.startDate();
    LocalDate endDate = request.endDate();

    ActivityData activityData = ViatorActivityAvailabilityMapper.mapToActivityData(activities, availabilities, startDate, endDate);

    return new ProviderActivityData(activities, activityData, startDate);
  }

  private static ViatorActivitySearchDTO buildActivitySearchDTO(ProviderPlanningRequest request) {
    ViatorActivitySearchDTO.Range ratingRange = new ViatorActivitySearchDTO.Range(4, 5);

    List<Integer> activityTagIds = request.activityTags().stream().map(Integer::valueOf).toList();

    ViatorActivitySearchDTO.Filtering filteringDTO =
        new ViatorActivitySearchDTO.Filtering(
            Long.valueOf(request.providerDestinationId()),
            activityTagIds,
            request.startDate(),
            request.endDate(),
            ratingRange);

    ViatorActivitySearchDTO.Sorting sorting =
        new ViatorActivitySearchDTO.Sorting(
            ViatorActivitySortingType.TRAVELER_RATING, ViatorActivitySortingOrder.DESCENDING);

    int durationDays = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate());

      return new ViatorActivitySearchDTO(
          filteringDTO, sorting, new ViatorActivitySearchDTO.Pagination(1, Math.max(durationDays, 1) * 4), "EUR");
  }

  private List<ViatorActivityDTO> getViatorActivityDTOS(SupportedLocale defaultLocale, ViatorActivitySearchDTO searchDTO) {
    return viatorClient.getActivitiesByRequestAndLanguage(defaultLocale.getCode(), searchDTO).stream()
            // No activities without duration information should be returned
            .filter(dto -> dto.getDurationMinutes() > 0)
            .toList();
  }

  public List<ViatorActivityAvailabilityDTO> getViatorAvailabilityDTOS(
      List<ViatorActivityDTO> activities) {

    Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> availabilityFutures =
        getIdCompletableFutureMap(activities);

    CompletableFuture.allOf(availabilityFutures.values().toArray(new CompletableFuture[0])).join();

    return activities.stream()
        .map(
            activity -> {
              Optional<ViatorActivityAvailabilityDTO> availabilityOpt =
                  availabilityFutures.get(activity.productCode()).join();

              return availabilityOpt
                  .orElse(null);
            })
        .filter(Objects::nonNull)
        .toList();
  }


  private Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> getIdCompletableFutureMap(
      List<ViatorActivityDTO> activities) {
    Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> availabilityFutures = new HashMap<>();
    for (ViatorActivityDTO activity : activities) {
      availabilityFutures.computeIfAbsent(
          activity.productCode(),
          productCode ->
              CompletableFuture.supplyAsync(() -> viatorClient.getAvailabilityByProductCode(productCode))
                  .exceptionally(
                      ex -> {
                        log.error("Error fetching activity availability for productCode: {}", productCode);
                        return Optional.empty();
                      }));
    }
    return availabilityFutures;
  }
}
