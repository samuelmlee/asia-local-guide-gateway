package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import com.asialocalguide.gateway.destination.domain.CommonDestination;
import com.asialocalguide.gateway.destination.domain.Coordinates;
import com.asialocalguide.gateway.destination.domain.Country;
import com.asialocalguide.gateway.destination.domain.Destination;
import com.asialocalguide.gateway.destination.domain.DestinationIngestionInput;
import com.asialocalguide.gateway.destination.domain.DestinationTranslation;
import com.asialocalguide.gateway.destination.domain.DestinationType;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.destination.dto.DestinationDTO;
import com.asialocalguide.gateway.destination.repository.DestinationRepository;
import com.asialocalguide.gateway.destination.service.DestinationService;
import com.asialocalguide.gateway.destination.service.DestinationSortingService;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;

@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {

	@Mock
	private DestinationProvider viatorProvider;

	@Mock
	private DestinationRepository destinationRepository;

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
	void syncDestinations_ForProvider_shouldProcessValidProviders() {
		// Arrange
		CommonDestination rawDto = createTestRawDestination();
		when(viatorProvider.getProviderName()).thenReturn(providerName);
		when(viatorProvider.getDestinations()).thenReturn(List.of(rawDto));

		destinationService = new DestinationService(List.of(viatorProvider),
				destinationSortingService,
				destinationRepository);

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

		destinationService = new DestinationService(List.of(viatorProvider),
				destinationSortingService,
				destinationRepository);

		// Act
		Throwable exception = assertThrows(ViatorApiException.class,
				() -> destinationService.syncDestinationsForProvider(BookingProviderName.VIATOR));

		// Assert
		assertThat(exception).hasMessageContaining("API failure");
	}

	@Test
	void getAutocompleteSuggestions_shouldReturnLocalizedResults() {
		// Arrange
		LocaleContextHolder.setLocale(Locale.FRANCE);
		Destination destination = createTestDestinationWithTranslations();

		when(destinationRepository.findCityOrRegionByNameWithEagerTranslations(LanguageCode.FR, "New York"))
				.thenReturn(List.of(destination));

		// Act
		List<DestinationDTO> result = destinationService.getAutocompleteSuggestions("New York");

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
		return new CommonDestination(testDestinationId,
				List.of(new CommonDestination.Translation(LanguageCode.EN, "New York")),
				DestinationType.CITY,
				null,
				providerName,
				countryIsoCode);
	}

	private Destination createTestDestinationWithTranslations() {
		Destination destination = new Destination(new Country("FR"),
				DestinationType.CITY,
				new Coordinates(40.7128, -74.0060));
		destination.addTranslation(new DestinationTranslation(destination, new Language(1L, LanguageCode.EN), "Paris"));
		destination.addTranslation(new DestinationTranslation(destination, new Language(2L, LanguageCode.FR), "Paris"));
		return destination;
	}
}
