package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.DestinationTranslation;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.DestinationDTO;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {

    @Mock
    private DestinationProvider viatorProvider;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private BookingProviderRepository bookingProviderRepository;

    @Mock
    private BookingProviderMappingRepository bookingProviderMappingRepository;

    @Mock
    private DestinationSortingService destinationSortingService;

    @InjectMocks
    private DestinationService destinationService;

    private final BookingProviderName providerName = BookingProviderName.VIATOR;
    private final String testDestinationId = "DEST-123";
    private final String countryIsoCode = "US";

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.US); // Default to EN
    }

    @Test
    void syncDestinations_shouldProcessValidProviders() {
        // Arrange
        RawDestinationDTO rawDto = createTestRawDestination();
        when(viatorProvider.getProviderName()).thenReturn(providerName);
        when(viatorProvider.getDestinations()).thenReturn(List.of(rawDto));
        when(bookingProviderRepository.findByName(providerName))
                .thenReturn(Optional.of(new BookingProvider()));
        when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderId(any()))
                .thenReturn(Set.of());

        destinationService = new DestinationService(
                List.of(viatorProvider),
                destinationSortingService,
                destinationRepository,
                bookingProviderRepository,
                bookingProviderMappingRepository
        );

        // Act
        destinationService.syncDestinations();

        // Assert
        verify(destinationSortingService).triageRawDestinations(anyMap());
    }

    @Test
    void syncDestinations_shouldHandleProviderExceptionsGracefully() {
        // Arrange
        when(viatorProvider.getProviderName()).thenReturn(providerName);
        when(viatorProvider.getDestinations()).thenThrow(new RuntimeException("API failure"));

        destinationService = new DestinationService(
                List.of(viatorProvider),
                destinationSortingService,
                destinationRepository,
                bookingProviderRepository,
                bookingProviderMappingRepository
        );

        // Act
        assertDoesNotThrow(() -> destinationService.syncDestinations());

        // Assert
        verify(destinationSortingService).triageRawDestinations(Map.of(BookingProviderName.VIATOR, Map.of()));
    }


    @Test
    void getAutocompleteSuggestions_shouldReturnLocalizedResults() {
        // Arrange
        LocaleContextHolder.setLocale(Locale.FRANCE);
        Destination destination = createTestDestinationWithTranslations();

        when(destinationRepository.findCityOrRegionByTranslationsForLanguageCodeAndName(
                LanguageCode.FR, "paris"))
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
        LocaleContextHolder.setLocale(Locale.of("xx")); // Unsupported locale

        Destination destination = createTestDestinationWithTranslations();
        when(destinationRepository.findCityOrRegionByTranslationsForLanguageCodeAndName(
                LanguageCode.EN, "test"))
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

    private RawDestinationDTO createTestRawDestination() {
        return new RawDestinationDTO(
                testDestinationId,
                List.of(new RawDestinationDTO.Translation("EN", "New York")),
                DestinationType.CITY,
                null,
                providerName,
                countryIsoCode
        );
    }

    private Destination createTestDestinationWithTranslations() {
        Destination destination = new Destination();
        destination.setType(DestinationType.CITY);
        destination.addTranslation(new DestinationTranslation(LanguageCode.EN, "New York"));
        destination.addTranslation(new DestinationTranslation(LanguageCode.FR, "Paris"));
        return destination;
    }
}