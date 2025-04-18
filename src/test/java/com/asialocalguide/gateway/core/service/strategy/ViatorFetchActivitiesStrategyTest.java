package com.asialocalguide.gateway.core.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.domain.planning.ActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderActivityData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViatorFetchActivitiesStrategyTest {

  @Mock private BookingProviderRepository bookingProviderRepository;

  @Mock private DestinationRepository destinationRepository;

  @Mock private ViatorActivityService viatorActivityService;

  @InjectMocks private ViatorFetchActivitiesStrategy strategy;

  private PlanningRequestDTO validRequest;
  private final LocalDate today = LocalDate.now();
  private final LocalDate tomorrow = today.plusDays(3);

  @BeforeEach
  void setUp() {
    validRequest = new PlanningRequestDTO(today, tomorrow, 1L, Collections.singletonList("adventure"));
  }

  @Test
  void getProviderName_shouldReturnViator() {
    assertThat(strategy.getProviderName()).isEqualTo(BookingProviderName.VIATOR);
  }

  @Test
  void fetchProviderActivity_shouldThrowWhenViatorProviderNotFound() {
    when(bookingProviderRepository.findByName(BookingProviderName.VIATOR)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.fetchProviderActivity(validRequest, LanguageCode.EN))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Viator BookingProvider not found");
  }

  @Test
  void fetchProviderActivity_shouldThrowWhenDestinationNotFound() {
    setupViatorProvider();
    when(destinationRepository.findById(validRequest.destinationId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.fetchProviderActivity(validRequest, LanguageCode.EN))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fetchProviderActivity_shouldThrowWhenNoViatorMapping() {
    BookingProvider viator = setupViatorProvider();
    Destination destination = mock(Destination.class);

    when(destinationRepository.findById(validRequest.destinationId())).thenReturn(Optional.of(destination));
    when(destination.getBookingProviderMapping(viator.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.fetchProviderActivity(validRequest, LanguageCode.EN))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Destination BookingProvider Mapping not found");
  }

  @Test
  void fetchProviderActivity_shouldPassCorrectParametersToService() {
    // Setup
    BookingProvider viator = setupViatorProvider();
    Destination destination = mock(Destination.class);
    DestinationProviderMapping mapping = new DestinationProviderMapping(destination, viator, "VIATOR_DEST_123");

    when(destinationRepository.findById(validRequest.destinationId())).thenReturn(Optional.of(destination));
    when(destination.getBookingProviderMapping(viator.getId())).thenReturn(Optional.of(mapping));

    // Correct initialization with ActivityData
    boolean[][][] availability = {{{true}}};
    String[][][] startTimes = {{{"06:00"}}};
    int[] ratings = {5};
    int[] durations = {1};

    ActivityData activityData = new ActivityData(availability, startTimes, ratings, durations);
    ProviderActivityData expectedData = new ProviderActivityData(null, activityData, validRequest.startDate());

    when(viatorActivityService.fetchProviderActivityData(any())).thenReturn(expectedData);

    // Execute
    ProviderActivityData result = strategy.fetchProviderActivity(validRequest, LanguageCode.FR);

    // Verify
    assertThat(result).isEqualTo(expectedData);

    ArgumentCaptor<ProviderPlanningRequest> captor = ArgumentCaptor.forClass(ProviderPlanningRequest.class);
    verify(viatorActivityService).fetchProviderActivityData(captor.capture());

    ProviderPlanningRequest request = captor.getValue();
    assertThat(request.providerDestinationId()).isEqualTo("VIATOR_DEST_123");
    assertThat(request.languageCode()).isEqualTo(LanguageCode.FR);
    assertThat(request.startDate()).isEqualTo(today);
    assertThat(request.endDate()).isEqualTo(tomorrow);
    assertThat(request.activityTags()).containsExactly("adventure");
  }

  @Test
  void fetchProviderActivity_shouldHandleServiceExceptions() {
    setupViatorProvider();
    Destination destination = mock(Destination.class);
    DestinationProviderMapping mapping =
        new DestinationProviderMapping(destination, new BookingProvider(), "VIATOR_DEST_123");

    when(destinationRepository.findById(validRequest.destinationId())).thenReturn(Optional.of(destination));
    when(destination.getBookingProviderMapping(any())).thenReturn(Optional.of(mapping));
    when(viatorActivityService.fetchProviderActivityData(any())).thenThrow(new RuntimeException("API Failure"));

    assertThatThrownBy(() -> strategy.fetchProviderActivity(validRequest, LanguageCode.EN))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("API Failure");
  }

  private BookingProvider setupViatorProvider() {
    BookingProvider viator = new BookingProvider();
    viator.setId(1L);
    viator.setName(BookingProviderName.VIATOR);
    when(bookingProviderRepository.findByName(BookingProviderName.VIATOR)).thenReturn(Optional.of(viator));
    return viator;
  }
}
