package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.destination.DestinationPersistenceService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DestinationPersistenceServiceTest {

  @Mock private DestinationRepository destinationRepository;
  @Mock private BookingProviderRepository bookingProviderRepository;
  @Mock private CountryRepository countryRepository;

  @InjectMocks private DestinationPersistenceService service;

  private BookingProvider provider;
  private final Long providerId = 1L;
  private final BookingProviderName providerName = BookingProviderName.VIATOR;

  @BeforeEach
  void setUp() {
    provider = new BookingProvider();
    provider.setId(providerId);
    provider.setName(providerName);
  }

  // Tests for persistExistingDestinations
  @Test
  void persistExistingDestinations_WhenProviderNotFound_ThrowsException() {
    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.empty());

    assertThrows(
        IllegalStateException.class,
        () -> service.persistExistingDestinations(providerName, Map.of(1L, mockRawDestinationDTO())));
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistExistingDestinations_WithEmptyMap_DoesNothing() {
    service.persistExistingDestinations(providerName, Map.of());

    verify(bookingProviderRepository, never()).findByName(any());
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistExistingDestinations_AddsMissingProviderMappings() {
    // Setup
    Long destinationId = 100L;
    CrossPlatformDestination rawDto =
        new CrossPlatformDestination("D123", List.of(), DestinationType.CITY, null, providerName, "US");
    Map<Long, CrossPlatformDestination> idToRawDestinations = Map.of(destinationId, rawDto);

    // Create mock Destination
    Destination existingDestination = mock(Destination.class);
    when(existingDestination.getId()).thenReturn(destinationId);

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(destinationRepository.findAllById(Set.of(destinationId))).thenReturn(List.of(existingDestination));

    // Execute
    service.persistExistingDestinations(providerName, idToRawDestinations);

    // Verify addProviderMapping was called once with correct arguments
    ArgumentCaptor<DestinationProviderMapping> captor = ArgumentCaptor.forClass(DestinationProviderMapping.class);
    verify(existingDestination, times(1)).addProviderMapping(captor.capture());

    DestinationProviderMapping addedMapping = captor.getValue();
    assertNotNull(addedMapping);
    assertEquals("D123", addedMapping.getProviderDestinationId());
    assertEquals(providerId, addedMapping.getProvider().getId());
  }

  @Test
  void persistExistingDestinations_SkipsWhenMappingExists() {
    Long destinationId = 100L;
    CrossPlatformDestination rawDto =
        new CrossPlatformDestination("D123", List.of(), DestinationType.CITY, null, providerName, "US");
    Map<Long, CrossPlatformDestination> idToRawDestinations = Map.of(destinationId, rawDto);

    Destination existingDestination = mock(Destination.class);

    DestinationProviderMapping existingMapping = new DestinationProviderMapping();
    existingMapping.setProviderDestinationId("D123");
    existingMapping.setProvider(provider);
    existingDestination.addProviderMapping(existingMapping);

    when(existingDestination.getBookingProviderMapping(1L)).thenReturn(existingMapping);
    when(existingDestination.getId()).thenReturn(destinationId);

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(destinationRepository.findAllById(Set.of(destinationId))).thenReturn(List.of(existingDestination));

    service.persistExistingDestinations(providerName, idToRawDestinations);

    // Verify addProviderMapping was called Once when setting up the mock
    verify(existingDestination, times(1)).addProviderMapping(any(DestinationProviderMapping.class));
  }

  // Tests for persistNewDestinations
  @Test
  void persistNewDestinations_WhenProviderNotFound_ThrowsException() {
    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.empty());

    assertThrows(
        IllegalStateException.class,
        () -> service.persistNewDestinations(providerName, Map.of("US", List.of(mockRawDestinationDTO()))));
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistNewDestinations_WithEmptyMap_DoesNothing() {
    service.persistNewDestinations(providerName, Map.of());

    verify(bookingProviderRepository, never()).findByName(any());
    verifyNoInteractions(countryRepository, destinationRepository);
  }

  @Test
  void persistNewDestinations_WhenCountryNotFound_SkipsDTOs() {
    String isoCode = "US";
    CrossPlatformDestination rawDto =
        new CrossPlatformDestination(
            "D123",
            List.of(new CrossPlatformDestination.Translation("en", "New York")),
            DestinationType.CITY,
            new Coordinates(),
            providerName,
            isoCode);
    Map<String, List<CrossPlatformDestination>> isoToDtos = Map.of(isoCode, List.of(rawDto));

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(isoCode))).thenReturn(List.of());

    service.persistNewDestinations(providerName, isoToDtos);

    verify(destinationRepository, never()).saveAll(any());
  }

  @Test
  void persistNewDestinations_CreatesNewDestinationsWithMappingsAndTranslations() {
    String isoCode = "us";
    Country country = new Country(isoCode);

    CrossPlatformDestination.Translation translation = new CrossPlatformDestination.Translation("en", "New York");
    CrossPlatformDestination rawDto =
        new CrossPlatformDestination(
            "D123",
            List.of(translation),
            DestinationType.CITY,
            new Coordinates(40.7128, -74.0060),
            providerName,
            isoCode);
    Map<String, List<CrossPlatformDestination>> isoToDtos = Map.of(isoCode, List.of(rawDto));

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(isoCode))).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, isoToDtos);

    // Verify saveAll is called with a list of one destination
    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    List<Destination> savedDestinations = captor.getValue();

    assertEquals(1, savedDestinations.size());
    Destination saved = savedDestinations.get(0);
    assertEquals(country, saved.getCountry());
    assertEquals(DestinationType.CITY, saved.getType());

    assertEquals(Optional.of("New York"), saved.getTranslation(LanguageCode.EN));

    assertNotNull(saved.getBookingProviderMapping(1L));

    assertEquals("D123", saved.getBookingProviderMapping(1L).getProviderDestinationId());
  }

  @Test
  void persistNewDestinations_WithNullDtoInList_SkipsNull() {
    String isoCode = "us";
    Country country = new Country(isoCode);

    Map<String, List<CrossPlatformDestination>> isoToDtos =
        Map.of(isoCode, Arrays.asList(null, mockRawDestinationDTO()));

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(isoCode))).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, isoToDtos);

    verify(destinationRepository).saveAll(anyList()); // Only one valid DTO is processed
  }

  @Test
  void persistExistingDestinations_NullProviderName_ExitsEarly() {

    service.persistExistingDestinations(null, Map.of(1L, mockRawDestinationDTO()));

    verifyNoInteractions(bookingProviderRepository, destinationRepository);
  }

  @Test
  void persistExistingDestinations_PartialDestinationMatches_ProcessesOnlyFound() {
    Long foundId = 1L;
    Long missingId = 2L;
    CrossPlatformDestination dto = mockRawDestinationDTO();
    Map<Long, CrossPlatformDestination> input = Map.of(foundId, dto, missingId, dto);

    // Create a mock Destination and stub getId()
    Destination foundDestination = mock(Destination.class);
    when(foundDestination.getId()).thenReturn(foundId);

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(destinationRepository.findAllById(Set.of(foundId, missingId))).thenReturn(List.of(foundDestination));

    service.persistExistingDestinations(providerName, input);

    ArgumentCaptor<DestinationProviderMapping> captor = ArgumentCaptor.forClass(DestinationProviderMapping.class);

    verify(foundDestination).addProviderMapping(captor.capture());

    DestinationProviderMapping addedMapping = captor.getValue();
    assertNotNull(addedMapping);
    assertEquals(dto.destinationId(), addedMapping.getProviderDestinationId());
    assertEquals(providerId, addedMapping.getProvider().getId());
  }

  @Test
  void persistExistingDestinations_NullRawDtoInMap_LogsWarning() {
    Long destinationId = 1L;
    Map<Long, CrossPlatformDestination> input = new HashMap<>();
    input.put(destinationId, null);

    assertThatThrownBy(() -> service.persistExistingDestinations(providerName, input))
        .isInstanceOf(IllegalStateException.class);

    // Verify no repository interactions
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistNewDestinations_NullProviderName_ExitsEarly() {

    service.persistNewDestinations(null, Map.of("US", List.of(mockRawDestinationDTO())));
    verifyNoInteractions(bookingProviderRepository, countryRepository, destinationRepository);
  }

  @Test
  void persistNewDestinations_MultipleCountriesWithMixedValidity() {
    String validIso = "US";
    String invalidIso = "XX";
    Country validCountry = new Country(validIso);

    Map<String, List<CrossPlatformDestination>> input =
        Map.of(
            validIso, List.of(mockRawDestinationDTO()),
            invalidIso, List.of(mockRawDestinationDTO()));

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(validIso, invalidIso))).thenReturn(List.of(validCountry));

    service.persistNewDestinations(providerName, input);

    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    assertEquals(1, captor.getValue().size()); // Only valid country processed
  }

  @Test
  void persistNewDestinations_WithNullCoordinates_SetsNullInEntity() {
    CrossPlatformDestination dto =
        new CrossPlatformDestination(
            "D123",
            List.of(new CrossPlatformDestination.Translation("en", "Test")),
            DestinationType.CITY,
            null, // Null coordinates
            providerName,
            "US");

    Country country = new Country("US");

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(anySet())).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, Map.of("US", List.of(dto)));

    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    assertNull(captor.getValue().get(0).getCenterCoordinates());
  }

  @Test
  void persistNewDestinations_WithMultipleTranslations_CreatesAll() {
    CrossPlatformDestination dto =
        new CrossPlatformDestination(
            "D123",
            List.of(
                new CrossPlatformDestination.Translation("en", "Singapore"),
                new CrossPlatformDestination.Translation("fr", "Singapour")),
            DestinationType.CITY,
            null,
            providerName,
            "US");

    Country country = new Country("US");

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(anySet())).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, Map.of("US", List.of(dto)));

    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    Destination destination = captor.getValue().getFirst();
    assertEquals(Optional.of("Singapore"), destination.getTranslation(LanguageCode.EN));
    assertEquals(Optional.of("Singapour"), destination.getTranslation(LanguageCode.FR));
  }

  @Test
  void persistNewDestinations_WithInvalidLanguageCode_ThrowsException() {
    CrossPlatformDestination dto =
        new CrossPlatformDestination(
            "D123",
            List.of(new CrossPlatformDestination.Translation("invalid", "Test")),
            DestinationType.CITY,
            null,
            providerName,
            "US");

    Country country = new Country("US");

    when(bookingProviderRepository.findByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(anySet())).thenReturn(List.of(country));

    assertThrows(
        IllegalArgumentException.class, () -> service.persistNewDestinations(providerName, Map.of("US", List.of(dto))));
  }

  private static CrossPlatformDestination mockRawDestinationDTO() {
    return new CrossPlatformDestination(
        "test-id",
        List.of(new CrossPlatformDestination.Translation("en", "Test")),
        DestinationType.CITY,
        null,
        BookingProviderName.VIATOR,
        "US");
  }
}
