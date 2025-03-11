package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorActivityAvailabilityMappingException;
import com.asialocalguide.gateway.viator.exception.ViatorActivityServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public class ViatorActivityService {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final int MIN_RATING = 4;
    private static final int MAX_RATING = 5;

    private final ViatorClient viatorClient;

    public ViatorActivityService(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    public ProviderActivityData fetchProviderActivityData(ProviderPlanningRequest request) {
        validatePlanningRequest(request);

        try {
            ViatorActivitySearchDTO searchDTO = buildActivitySearchDTO(request);
            List<ViatorActivityDTO> activities = fetchValidActivities(request.locale(), searchDTO);
            List<ViatorActivityAvailabilityDTO> availabilities = fetchActivityAvailabilities(activities);

            return new ProviderActivityData(
                    Collections.unmodifiableList(activities),
                    mapToActivityData(activities, availabilities, request),
                    request.startDate()
            );
        } catch (Exception e) {
            throw new ViatorActivityServiceException("Failed to fetch activity data", e);
        }
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
                        new ViatorActivitySearchDTO.Range(MIN_RATING, MAX_RATING)
                ),
                new ViatorActivitySearchDTO.Sorting(
                        ViatorActivitySortingType.TRAVELER_RATING,
                        ViatorActivitySortingOrder.DESCENDING
                ),
                createPagination(request),
                DEFAULT_CURRENCY
        );
    }

    private List<Integer> convertActivityTags(List<String> tags) {
        return Optional.ofNullable(tags)
                .orElse(Collections.emptyList())
                .stream()
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

    private List<ViatorActivityDTO> fetchValidActivities(SupportedLocale locale, ViatorActivitySearchDTO searchDTO) {
        return Optional.ofNullable(viatorClient.getActivitiesByRequestAndLanguage(
                        requireNonNull(locale, "Locale must not be null").getCode(),
                        requireNonNull(searchDTO, "SearchDTO must not be null")))
                .orElse(Collections.emptyList())
                .stream()
                .filter(dto -> dto.getDurationMinutes() > 0)
                .toList();
    }

    private List<ViatorActivityAvailabilityDTO> fetchActivityAvailabilities(List<ViatorActivityDTO> activities) {
        if (activities == null || activities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> futures =
                createAvailabilityFutures(activities);

        return futures.entrySet().parallelStream()
                .map(entry -> handleAvailabilityFuture(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> createAvailabilityFutures(
            List<ViatorActivityDTO> activities) {

        Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> futures = new ConcurrentHashMap<>();
        activities.forEach(activity -> {
            String productCode = requireNonNull(activity.productCode(), "Product code must not be null");
            futures.put(productCode, fetchAvailabilityAsync(productCode));
        });
        return futures;
    }

    private CompletableFuture<Optional<ViatorActivityAvailabilityDTO>> fetchAvailabilityAsync(String productCode) {
        return CompletableFuture
                .supplyAsync(() -> viatorClient.getAvailabilityByProductCode(productCode))
                .exceptionally(ex -> {
                    log.error("Error fetching availability for productCode: {}", productCode, ex);
                    return Optional.empty();
                });
    }

    private Optional<ViatorActivityAvailabilityDTO> handleAvailabilityFuture(
            String productCode, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>> future) {
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            log.error("Failed to fetch availability for productCode: {}", productCode, e);
            return Optional.empty();
        }
    }

    private ActivityData mapToActivityData(List<ViatorActivityDTO> activities,
                                           List<ViatorActivityAvailabilityDTO> availabilities,
                                           ProviderPlanningRequest request) {
        try {
            return ViatorActivityAvailabilityMapper.mapToActivityData(
                    activities,
                    availabilities,
                    request.startDate(),
                    request.endDate()
            );
        } catch (ViatorActivityAvailabilityMappingException e) {
            log.error("Failed to map activity data: {}", e.getMessage());
            throw new ViatorActivityServiceException("Activity data mapping failed", e);
        }
    }

}