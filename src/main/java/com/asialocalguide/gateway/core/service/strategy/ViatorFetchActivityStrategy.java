package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.CommonPersistableActivity;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@link FetchActivityStrategy} implementation for the Viator booking provider.
 *
 * <p>Delegates to {@link ViatorActivityService} to retrieve activity details by provider ID.
 */
@Component
@Slf4j
public class ViatorFetchActivityStrategy implements FetchActivityStrategy {

	private static final BookingProviderName providerName = BookingProviderName.VIATOR;

	private final ViatorActivityService viatorActivityService;

	/**
	 * @param viatorActivityService service handling Viator API calls for activity data
	 */
	public ViatorFetchActivityStrategy(ViatorActivityService viatorActivityService) {
		this.viatorActivityService = viatorActivityService;
	}

	/** {@inheritDoc} */
	@Override
	public BookingProviderName getProviderName() {
		return providerName;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if {@code activityIds} is {@code null}
	 */
	@Override
	public List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds) {
		if (activityIds == null) {
			throw new IllegalArgumentException("Activity IDs cannot be null");
		}

		if (activityIds.isEmpty()) {
			return List.of();
		}

		return viatorActivityService.fetchProviderActivities(activityIds);
	}
}
