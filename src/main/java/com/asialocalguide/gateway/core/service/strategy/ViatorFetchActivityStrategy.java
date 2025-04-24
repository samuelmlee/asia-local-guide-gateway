package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ViatorFetchActivityStrategy implements FetchActivityStrategy {

  private static final BookingProviderName providerName = BookingProviderName.VIATOR;

  private final ViatorActivityService viatorActivityService;

  public ViatorFetchActivityStrategy(ViatorActivityService viatorActivityService) {
    this.viatorActivityService = viatorActivityService;
  }

  @Override
  public BookingProviderName getProviderName() {
    return providerName;
  }

  @Override
  public List<CommonPersistableActivity> fetchProviderActivities(Set<String> activityIds) {
    return viatorActivityService.fetchProviderActivities(activityIds);
  }
}
