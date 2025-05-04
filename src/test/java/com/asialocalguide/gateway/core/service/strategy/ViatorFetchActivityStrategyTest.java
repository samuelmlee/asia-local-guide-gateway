package com.asialocalguide.gateway.core.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonPersistableActivity;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViatorFetchActivityStrategyTest {

  @Mock private ViatorActivityService viatorActivityService;

  @Captor ArgumentCaptor<Set<String>> activityIdsCaptor;

  @InjectMocks private ViatorFetchActivityStrategy strategy;

  @Test
  void getProviderName_shouldReturnViator() {
    assertThat(strategy.getProviderName()).isEqualTo(BookingProviderName.VIATOR);
  }

  @Test
  void fetchProviderActivities_shouldThrowExceptionWhenActivityIdsIsNull() {
    assertThatThrownBy(() -> strategy.fetchProviderActivities(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Activity IDs cannot be null");
  }

  @Test
  void fetchProviderActivities_shouldReturnEmptyListWhenActivityIdsIsEmpty() {
    List<CommonPersistableActivity> result = strategy.fetchProviderActivities(Set.of());

    assertThat(result).isEmpty();
    verifyNoInteractions(viatorActivityService);
  }

  @Test
  void fetchProviderActivities_shouldDelegateToServiceWhenActivityIdsHasValues() {
    Set<String> activityIds = Set.of("activity1", "activity2");

    strategy.fetchProviderActivities(activityIds);
    verify(viatorActivityService).fetchProviderActivities(activityIdsCaptor.capture());

    assertThat(activityIdsCaptor.getValue()).isEqualTo(activityIds);
  }

  @Test
  void fetchProviderActivities_shouldPropagateExceptionsFromService() {
    Set<String> activityIds = Set.of("activity1");
    RuntimeException expectedException = new RuntimeException("Service error");

    when(viatorActivityService.fetchProviderActivities(activityIds)).thenThrow(expectedException);

    assertThatThrownBy(() -> strategy.fetchProviderActivities(activityIds)).isSameAs(expectedException);
  }
}
