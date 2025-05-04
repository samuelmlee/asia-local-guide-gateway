package com.asialocalguide.gateway.core.service.strategy;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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
    if (activityIds == null) {
      throw new IllegalArgumentException("Activity IDs cannot be null");
    }

    if (activityIds.isEmpty()) {
      return List.of();
    }

    return viatorActivityService.fetchProviderActivities(activityIds);
  }
}
