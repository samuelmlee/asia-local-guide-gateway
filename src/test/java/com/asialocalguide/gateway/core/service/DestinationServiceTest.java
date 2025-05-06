package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import com.asialocalguide.gateway.core.service.destination.DestinationService;
import com.asialocalguide.gateway.core.service.destination.DestinationSortingService;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {

  @Mock private DestinationProvider viatorProvider;

  @Mock private DestinationRepository destinationRepository;

  @Mock private DestinationSortingService destinationSortingService;

  @InjectMocks private DestinationService destinationService;

  private final BookingProviderName providerName = BookingProviderName.VIATOR;
  private final String testDestinationId = "DEST-123";
  private final String countryIsoCode = "US";

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(Locale.US); // Default to EN
  }

  @Test
  void syncDestinations_ForProvider_shouldProcessValidProviders() {
    // Arrange
    CommonDestination rawDto = createTestRawDestination();
    when(viatorProvider.getProviderName()).thenReturn(providerName);
    when(viatorProvider.getDestinations()).thenReturn(List.of(rawDto));

    destinationService =
        new DestinationService(List.of(viatorProvider), destinationSortingService, destinationRepository);

    // Act
    destinationService.syncDestinationsForProvider(BookingProviderName.VIATOR);

    // Assert
    verify(destinationSortingService)
        .triageRawDestinations(new DestinationIngestionInput(providerName, List.of(rawDto)));
  }

  @Test
  void syncDestinations_ForProvider_shouldHandleProviderExceptionsGracefully() {
    // Arrange
    when(viatorProvider.getProviderName()).thenReturn(providerName);
    when(viatorProvider.getDestinations()).thenThrow(new ViatorApiException("API failure"));

    destinationService =
        new DestinationService(List.of(viatorProvider), destinationSortingService, destinationRepository);

    // Act
    Throwable exception =
        assertThrows(
            ViatorApiException.class, () -> destinationService.syncDestinationsForProvider(BookingProviderName.VIATOR));

    // Assert
    assertThat(exception).hasMessageContaining("API failure");
  }

  @Test
  void getAutocompleteSuggestions_shouldReturnLocalizedResults() {
    // Arrange
    LocaleContextHolder.setLocale(Locale.FRANCE);
    Destination destination = createTestDestinationWithTranslations();

    when(destinationRepository.findCityOrRegionByNameWithEagerTranslations(LanguageCode.FR, "paris"))
        .thenReturn(List.of(destination));

    // Act
    List<DestinationDTO> result = destinationService.getAutocompleteSuggestions("paris");

    // Assert
    assertEquals(1, result.size());
    assertEquals("Paris", result.getFirst().name());
  }

  @Test
  void getAutocompleteSuggestions_shouldHandleInvalidLocale() {
    // Arrange
    LocaleContextHolder.setLocale(Locale.of("xx")); // Unsupported languageCode

    Destination destination = createTestDestinationWithTranslations();
    when(destinationRepository.findCityOrRegionByNameWithEagerTranslations(LanguageCode.EN, "test"))
        .thenReturn(List.of(destination));

    // Act
    List<DestinationDTO> result = destinationService.getAutocompleteSuggestions("test");

    // Assert
    assertFalse(result.isEmpty());
  }

  @Test
  void getAutocompleteSuggestions_shouldReturnEmptyForBlankQuery() {
    // Act
    List<DestinationDTO> result1 = destinationService.getAutocompleteSuggestions("");
    List<DestinationDTO> result2 = destinationService.getAutocompleteSuggestions("   ");

    // Assert
    assertTrue(result1.isEmpty());
    assertTrue(result2.isEmpty());
  }

  private CommonDestination createTestRawDestination() {
    return new CommonDestination(
        testDestinationId,
        List.of(new CommonDestination.Translation("EN", "New York")),
        DestinationType.CITY,
        null,
        providerName,
        countryIsoCode);
  }

  private Destination createTestDestinationWithTranslations() {
    Destination destination = new Destination();
    destination.setType(DestinationType.CITY);
    destination.addTranslation(new DestinationTranslation(destination, new Language(1L, LanguageCode.EN), "New York"));
    destination.addTranslation(new DestinationTranslation(destination, new Language(2L, LanguageCode.FR), "Paris"));
    return destination;
  }
}
