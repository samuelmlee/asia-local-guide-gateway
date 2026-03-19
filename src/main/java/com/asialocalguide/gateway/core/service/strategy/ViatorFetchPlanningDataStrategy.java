package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import com.asialocalguide.gateway.destination.domain.Destination;
import com.asialocalguide.gateway.destination.domain.DestinationProviderMapping;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.destination.service.DestinationService;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningData;
import com.asialocalguide.gateway.planning.domain.ProviderPlanningRequest;
import com.asialocalguide.gateway.planning.dto.PlanningRequestDTO;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import org.springframework.stereotype.Component;

/**
 * {@link FetchPlanningDataStrategy} implementation for the Viator booking provider.
 *
 * <p>Resolves the Viator destination ID for the requested destination, constructs a
 * {@link ProviderPlanningRequest}, and delegates to {@link ViatorActivityService} to
 * fetch available activities and time slots.
 */
@Component
public class ViatorFetchPlanningDataStrategy implements FetchPlanningDataStrategy {

	private static final BookingProviderName PROVIDER_NAME = BookingProviderName.VIATOR;

	private final BookingProviderService bookingProviderService;

	private final DestinationService destinationService;

	private final ViatorActivityService viatorActivityService;

	/**
	 * @param bookingProviderService service for looking up the Viator {@link BookingProvider} record
	 * @param destinationService     service for resolving destinations and their provider mappings
	 * @param viatorActivityService  service handling Viator API calls for planning data
	 */
	public ViatorFetchPlanningDataStrategy(BookingProviderService bookingProviderService,
			DestinationService destinationService, ViatorActivityService viatorActivityService) {
		this.bookingProviderService = bookingProviderService;
		this.destinationService = destinationService;
		this.viatorActivityService = viatorActivityService;
	}

	/** {@inheritDoc} */
	@Override
	public BookingProviderName getProviderName() {
		return PROVIDER_NAME;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalStateException    if the Viator {@link BookingProvider} or destination provider mapping is not found
	 * @throws IllegalArgumentException if the destination does not exist
	 */
	@Override
	public ProviderPlanningData fetchProviderPlanningData(PlanningRequestDTO request, LanguageCode languageCode) {
		BookingProvider viatorProvider = bookingProviderService.getBookingProviderByName(PROVIDER_NAME)
				.orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

		Destination destination = destinationService.findDestinationById(request.destinationId())
				.orElseThrow(IllegalArgumentException::new);

		DestinationProviderMapping providerMapping = destination.getBookingProviderMapping(viatorProvider.getId())
				.orElseThrow(() -> new IllegalStateException("Destination BookingProvider Mapping not found"));

		String viatorDestinationId = providerMapping.getProviderDestinationId();

		ProviderPlanningRequest providerRequest = new ProviderPlanningRequest(request.startDate(),
				request.endDate(),
				request.getDuration(),
				request.activityTagIds(),
				viatorDestinationId,
				languageCode);

		return viatorActivityService.fetchProviderPlanningData(providerRequest);
	}
}
