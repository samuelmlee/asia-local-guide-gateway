package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import org.springframework.stereotype.Component;

@Component
public class ViatorFetchActivitiesStrategy implements FetchActivitiesStrategy {

  private static final BookingProviderName providerName = BookingProviderName.VIATOR;

  private final BookingProviderRepository bookingProviderRepository;

  private final DestinationRepository destinationRepository;

  private final ViatorActivityService viatorActivityService;

  public ViatorFetchActivitiesStrategy(
      BookingProviderRepository bookingProviderRepository,
      DestinationRepository destinationRepository,
      ViatorActivityService viatorActivityService) {
    this.bookingProviderRepository = bookingProviderRepository;
    this.destinationRepository = destinationRepository;
    this.viatorActivityService = viatorActivityService;
  }

  @Override
  public BookingProviderName getProviderName() {
    return providerName;
  }

  @Override
  public ProviderActivityPlanningData fetchProviderActivity(PlanningRequestDTO request, LanguageCode languageCode) {
    BookingProvider viatorProvider =
        bookingProviderRepository
            .findByName(BookingProviderName.VIATOR)
            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

    Destination destination =
        destinationRepository.findById(request.destinationId()).orElseThrow(IllegalArgumentException::new);

    DestinationProviderMapping providerMapping =
        destination
            .getBookingProviderMapping(viatorProvider.getId())
            .orElseThrow(() -> new IllegalStateException("Destination BookingProvider Mapping not found"));

    String viatorDestinationId = providerMapping.getProviderDestinationId();

    ProviderPlanningRequest providerRequest =
        new ProviderPlanningRequest(
            request.startDate(),
            request.endDate(),
            request.getDuration(),
            request.activityTagIds(),
            viatorDestinationId,
            languageCode);

    return viatorActivityService.fetchProviderActivityData(providerRequest);
  }
}
