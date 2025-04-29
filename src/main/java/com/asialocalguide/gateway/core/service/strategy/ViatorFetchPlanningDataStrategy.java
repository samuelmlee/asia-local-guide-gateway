package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import com.asialocalguide.gateway.core.service.destination.DestinationService;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import org.springframework.stereotype.Component;

@Component
public class ViatorFetchPlanningDataStrategy implements FetchPlanningDataStrategy {

  private static final BookingProviderName providerName = BookingProviderName.VIATOR;

  private final BookingProviderService bookingProviderService;

  private final DestinationService destinationService;

  private final ViatorActivityService viatorActivityService;

  public ViatorFetchPlanningDataStrategy(
      BookingProviderService bookingProviderService,
      DestinationService destinationService,
      ViatorActivityService viatorActivityService) {
    this.bookingProviderService = bookingProviderService;
    this.destinationService = destinationService;
    this.viatorActivityService = viatorActivityService;
  }

  @Override
  public BookingProviderName getProviderName() {
    return providerName;
  }

  @Override
  public ProviderPlanningData fetchProviderPlanningData(PlanningRequestDTO request, LanguageCode languageCode) {
    BookingProvider viatorProvider =
        bookingProviderService
            .getBookingProviderByName(BookingProviderName.VIATOR)
            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

    Destination destination =
        destinationService.findDestinationById(request.destinationId()).orElseThrow(IllegalArgumentException::new);

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

    return viatorActivityService.fetchProviderPlanningData(providerRequest);
  }
}
