package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.dto.planning.ActivityPlanningRequestDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

  private final DestinationRepository destinationRepository;

  private final BookingProviderRepository bookingProviderRepository;

  private final ViatorActivityService viatorActivityService;

  public ActivityService(
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      ViatorActivityService viatorActivityService) {
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.viatorActivityService = viatorActivityService;
  }

  public List<ViatorActivityDetailDTO> getActivities(
      SupportedLocale locale, ActivityPlanningRequestDTO request) {

    // TODO: ActivityService should return a list of activities that is independent of the provider

    BookingProvider viatorProvider =
        bookingProviderRepository
            .findByName(BookingProviderName.VIATOR)
            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

    Destination destination =
        destinationRepository
            .findById(request.destinationId())
            .orElseThrow(IllegalArgumentException::new);

    String viatorDestinationId =
        destination.getBookingProviderMapping(viatorProvider.getId()).getProviderDestinationId();

    ViatorActivitySearchDTO.Range ratingRange = new ViatorActivitySearchDTO.Range(4, 5);

    ViatorActivitySearchDTO.Filtering filteringDTO =
        new ViatorActivitySearchDTO.Filtering(
            Long.valueOf(viatorDestinationId),
            request.activityTagIds(),
            request.startDate(),
            request.endDate(),
            ratingRange);

    ViatorActivitySearchDTO.Sorting sorting =
        new ViatorActivitySearchDTO.Sorting(
            ViatorActivitySortingType.TRAVELER_RATING, ViatorActivitySortingOrder.DESCENDING);

    int durationDays = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate());

    ViatorActivitySearchDTO searchDTO =
        new ViatorActivitySearchDTO(
            filteringDTO,
            sorting,
            new ViatorActivitySearchDTO.Pagination(1, Math.max(durationDays, 1) * 4),
            "EUR");

    return viatorActivityService.getActivityDetails(locale, searchDTO);
  }
}
