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
import com.asialocalguide.gateway.core.domain.planning.ActivityPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningData;
import com.asialocalguide.gateway.core.domain.planning.ProviderPlanningRequest;
import com.asialocalguide.gateway.core.dto.planning.PlanningRequestDTO;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
import com.asialocalguide.gateway.core.service.destination.DestinationService;
import com.asialocalguide.gateway.viator.service.ViatorActivityService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViatorFetchPlanningDataStrategyTest {

  @Mock private BookingProviderService bookingProviderService;

  @Mock private DestinationService destinationService;

  @Mock private ViatorActivityService viatorActivityService;

  @InjectMocks private ViatorFetchPlanningDataStrategy strategy;

  private PlanningRequestDTO validRequest;
  private final LocalDate today = LocalDate.now();
  private final LocalDate tomorrow = today.plusDays(3);

  @BeforeEach
  void setUp() {
    validRequest = new PlanningRequestDTO(today, tomorrow, UUID.randomUUID(), Collections.singletonList("adventure"));
  }

  @Test
  void getProviderName_shouldReturnViator() {
    assertThat(strategy.getProviderName()).isEqualTo(BookingProviderName.VIATOR);
  }

  @Test
  void fetchProviderActivity_shouldThrowWhenViatorProviderNotFound() {
    when(bookingProviderService.getBookingProviderByName(BookingProviderName.VIATOR)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.fetchProviderPlanningData(validRequest, LanguageCode.EN))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Viator BookingProvider not found");
  }

  @Test
  void fetchProviderPlanningData_shouldThrowWhenDestinationNotFound() {
    setupViatorProvider();
    when(destinationService.findDestinationById(validRequest.destinationId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.fetchProviderPlanningData(validRequest, LanguageCode.EN))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fetchProviderPlanningData_shouldThrowWhenNoViatorMapping() {
    BookingProvider viator = setupViatorProvider();
    Destination destination = mock(Destination.class);

    when(destinationService.findDestinationById(validRequest.destinationId())).thenReturn(Optional.of(destination));
    when(destination.getBookingProviderMapping(viator.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.fetchProviderPlanningData(validRequest, LanguageCode.EN))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Destination BookingProvider Mapping not found");
  }

  @Test
  void fetchProviderPlanningData_shouldPassCorrectParametersToService() {
    // Setup
    BookingProvider viator = setupViatorProvider();
    Destination destination = mock(Destination.class);
    when(destination.getId()).thenReturn(validRequest.destinationId());
    DestinationProviderMapping mapping = new DestinationProviderMapping(destination, viator, "VIATOR_DEST_123");

    when(destinationService.findDestinationById(validRequest.destinationId())).thenReturn(Optional.of(destination));
    when(destination.getBookingProviderMapping(viator.getId())).thenReturn(Optional.of(mapping));

    // Correct initialization with ActivityData
    boolean[][][] availability = {{{true}}};
    String[][][] startTimes = {{{"06:00"}}};
    int[] ratings = {5};
    int[] durations = {1};

    ActivityPlanningData activityPlanningData = new ActivityPlanningData(availability, startTimes, ratings, durations);
    ProviderPlanningData expectedData = new ProviderPlanningData(null, activityPlanningData, validRequest.startDate());

    when(viatorActivityService.fetchProviderPlanningData(any())).thenReturn(expectedData);

    // Execute
    ProviderPlanningData result = strategy.fetchProviderPlanningData(validRequest, LanguageCode.FR);

    // Verify
    assertThat(result).isEqualTo(expectedData);

    ArgumentCaptor<ProviderPlanningRequest> captor = ArgumentCaptor.forClass(ProviderPlanningRequest.class);
    verify(viatorActivityService).fetchProviderPlanningData(captor.capture());

    ProviderPlanningRequest request = captor.getValue();
    assertThat(request.providerDestinationId()).isEqualTo("VIATOR_DEST_123");
    assertThat(request.languageCode()).isEqualTo(LanguageCode.FR);
    assertThat(request.startDate()).isEqualTo(today);
    assertThat(request.endDate()).isEqualTo(tomorrow);
    assertThat(request.activityTags()).containsExactly("adventure");
  }

  @Test
  void fetchProviderPlanningData_shouldHandleServiceExceptions() {
    setupViatorProvider();
    Destination destination = mock(Destination.class);
    when(destination.getId()).thenReturn(validRequest.destinationId());
    DestinationProviderMapping mapping =
        new DestinationProviderMapping(
            destination, new BookingProvider(1L, BookingProviderName.VIATOR), "VIATOR_DEST_123");
    when(destination.getBookingProviderMapping(any())).thenReturn(Optional.of(mapping));

    when(destinationService.findDestinationById(validRequest.destinationId())).thenReturn(Optional.of(destination));
    when(viatorActivityService.fetchProviderPlanningData(any())).thenThrow(new RuntimeException("API Failure"));

    assertThatThrownBy(() -> strategy.fetchProviderPlanningData(validRequest, LanguageCode.EN))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("API Failure");
  }

  private BookingProvider setupViatorProvider() {
    BookingProvider viator = new BookingProvider(1L, BookingProviderName.VIATOR);
    when(bookingProviderService.getBookingProviderByName(BookingProviderName.VIATOR)).thenReturn(Optional.of(viator));
    return viator;
  }
}
