package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.core.service.composer.ActivityProvider;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorActivityAvailabilityMappingException;
import com.asialocalguide.gateway.viator.exception.ViatorActivityServiceException;
import com.asialocalguide.gateway.viator.util.ViatorActivityAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

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
        return null;
    }

    @Override
    public ProviderActivityData fetchProviderActivityData(ProviderPlanningRequest request) {
        validatePlanningRequest(request);

        try {
            ViatorActivitySearchDTO searchDTO = buildActivitySearchDTO(request);
            Map<String, ViatorActivityDTO> idToActivities = fetchValidActivities(request.locale(), searchDTO);
            List<ViatorActivityAvailabilityDTO> availabilities = fetchActivityAvailabilities(idToActivities.values());

            List<ViatorActivityDTO> activitiesToProcess = filterNoDataActivities(idToActivities, availabilities);

            List<CommonActivity> commonActivities = activitiesToProcess.stream().map(ViatorActivityAdapter::toCommon).toList();

            return new ProviderActivityData(
                    commonActivities,
                    mapToActivityData(activitiesToProcess, availabilities, request),
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

    private Map<String, ViatorActivityDTO> fetchValidActivities(SupportedLocale locale, ViatorActivitySearchDTO searchDTO) {
        return viatorClient.getActivitiesByRequestAndLanguage(
                        requireNonNull(locale, "Locale must not be null").getCode(),
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

        List<CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> futures =
                createAvailabilityFutures(activities);

        var futureArray = futures.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(futureArray)
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList())
                .join();
    }

    private List<CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> createAvailabilityFutures(
            Collection<ViatorActivityDTO> activities) {

        return activities.stream().map(activity -> {
            String productCode = requireNonNull(activity.productCode());

            return CompletableFuture
                    .supplyAsync(() -> viatorClient.getAvailabilityByProductCode(productCode))
                    .exceptionally(ex -> {
                        log.warn("Error while fetching Activity Availability for product code : {}", productCode);
                        return Optional.empty();
                    });
        }).toList();
    }

    private List<ViatorActivityDTO> filterNoDataActivities(Map<String, ViatorActivityDTO> idToActivities, List<ViatorActivityAvailabilityDTO> availabilities) {

        return availabilities.stream().map(availability -> idToActivities.get(availability.productCode()))
                .filter(Objects::nonNull)
                .toList();
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