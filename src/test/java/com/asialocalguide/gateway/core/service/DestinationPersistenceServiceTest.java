package com.asialocalguide.gateway.core.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.bookingprovider.BookingProviderService;
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
  @Mock private BookingProviderService bookingProviderService;
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
    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.empty());

    CommonDestination destination = mockRawDestinationDTO();
    Map<Long, CommonDestination> input = Map.of(1L, destination);

    try {
      service.persistExistingDestinations(providerName, input);
      fail("Expected IllegalStateException was not thrown");
    } catch (IllegalStateException e) {
      // Expected exception
      verifyNoInteractions(destinationRepository);
    }
  }

  @Test
  void persistExistingDestinations_WithEmptyMap_DoesNothing() {
    service.persistExistingDestinations(providerName, Map.of());

    verify(bookingProviderService, never()).getBookingProviderByName(any());
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistExistingDestinations_AddsMissingProviderMappings() {
    // Setup
    Long destinationId = 100L;
    CommonDestination rawDto =
        new CommonDestination(
            "D123",
            List.of(new CommonDestination.Translation("en", "Paris")),
            DestinationType.CITY,
            null,
            providerName,
            "US");
    Map<Long, CommonDestination> idToRawDestinations = Map.of(destinationId, rawDto);

    // Create mock Destination
    Destination existingDestination = mock(Destination.class);
    when(existingDestination.getId()).thenReturn(destinationId);

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
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
    CommonDestination rawDto =
        new CommonDestination(
            "D123",
            List.of(new CommonDestination.Translation("en", "Paris")),
            DestinationType.CITY,
            null,
            providerName,
            "US");
    Map<Long, CommonDestination> idToRawDestinations = Map.of(destinationId, rawDto);

    Destination existingDestination = mock(Destination.class);

    DestinationProviderMapping existingMapping = new DestinationProviderMapping(existingDestination, provider, "D123");
    existingDestination.addProviderMapping(existingMapping);

    when(existingDestination.getBookingProviderMapping(1L)).thenReturn(Optional.of(existingMapping));
    when(existingDestination.getId()).thenReturn(destinationId);

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(destinationRepository.findAllById(Set.of(destinationId))).thenReturn(List.of(existingDestination));

    service.persistExistingDestinations(providerName, idToRawDestinations);

    // Verify addProviderMapping was called Once when setting up the mock
    verify(existingDestination, times(1)).addProviderMapping(any(DestinationProviderMapping.class));
  }

  // Tests for persistNewDestinations
  @Test
  void persistNewDestinations_WhenProviderNotFound_ThrowsException() {
    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.empty());

    CommonDestination destination = mockRawDestinationDTO();
    Map<String, List<CommonDestination>> isoToDestinations = Map.of("US", List.of(destination));

    assertThrows(IllegalStateException.class, () -> service.persistNewDestinations(providerName, isoToDestinations));
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistNewDestinations_WithEmptyMap_DoesNothing() {
    service.persistNewDestinations(providerName, Map.of());

    verify(bookingProviderService, never()).getBookingProviderByName(any());
    verifyNoInteractions(countryRepository, destinationRepository);
  }

  @Test
  void persistNewDestinations_WhenCountryNotFound_SkipsDTOs() {
    String isoCode = "US";
    CommonDestination rawDto =
        new CommonDestination(
            "D123",
            List.of(new CommonDestination.Translation("en", "New York")),
            DestinationType.CITY,
            new Coordinates(),
            providerName,
            isoCode);
    Map<String, List<CommonDestination>> isoToDtos = Map.of(isoCode, List.of(rawDto));

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(isoCode))).thenReturn(List.of());

    service.persistNewDestinations(providerName, isoToDtos);

    verify(destinationRepository, never()).saveAll(any());
  }

  @Test
  void persistNewDestinations_CreatesNewDestinationsWithMappingsAndTranslations() {
    String isoCode = "us";
    Country country = new Country(isoCode);

    CommonDestination.Translation translation = new CommonDestination.Translation("en", "New York");
    CommonDestination rawDto =
        new CommonDestination(
            "D123",
            List.of(translation),
            DestinationType.CITY,
            new Coordinates(40.7128, -74.0060),
            providerName,
            isoCode);
    Map<String, List<CommonDestination>> isoToDtos = Map.of(isoCode, List.of(rawDto));

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
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

    assertTrue(saved.getBookingProviderMapping(1L).isPresent());

    assertEquals("D123", saved.getBookingProviderMapping(1L).get().getProviderDestinationId());
  }

  @Test
  void persistNewDestinations_WithNullDtoInList_SkipsNull() {
    String isoCode = "us";
    Country country = new Country(isoCode);

    Map<String, List<CommonDestination>> isoToDtos = Map.of(isoCode, Arrays.asList(null, mockRawDestinationDTO()));

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(isoCode))).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, isoToDtos);

    verify(destinationRepository).saveAll(anyList()); // Only one valid DTO is processed
  }

  @Test
  void persistExistingDestinations_NullProviderName_ExitsEarly() {

    service.persistExistingDestinations(null, Map.of(1L, mockRawDestinationDTO()));

    verifyNoInteractions(bookingProviderService, destinationRepository);
  }

  @Test
  void persistExistingDestinations_PartialDestinationMatches_ProcessesOnlyFound() {
    Long foundId = 1L;
    Long missingId = 2L;
    CommonDestination dto = mockRawDestinationDTO();
    Map<Long, CommonDestination> input = Map.of(foundId, dto, missingId, dto);

    // Create a mock Destination and stub getId()
    Destination foundDestination = mock(Destination.class);
    when(foundDestination.getId()).thenReturn(foundId);

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
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
    Map<Long, CommonDestination> input = new HashMap<>();
    input.put(destinationId, null);

    assertThatThrownBy(() -> service.persistExistingDestinations(providerName, input))
        .isInstanceOf(IllegalStateException.class);

    // Verify no repository interactions
    verifyNoInteractions(destinationRepository);
  }

  @Test
  void persistNewDestinations_NullProviderName_ExitsEarly() {

    service.persistNewDestinations(null, Map.of("US", List.of(mockRawDestinationDTO())));
    verifyNoInteractions(bookingProviderService, countryRepository, destinationRepository);
  }

  @Test
  void persistNewDestinations_MultipleCountriesWithMixedValidity() {
    String validIso = "US";
    String invalidIso = "XX";
    Country validCountry = new Country(validIso);

    Map<String, List<CommonDestination>> input =
        Map.of(
            validIso, List.of(mockRawDestinationDTO()),
            invalidIso, List.of(mockRawDestinationDTO()));

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(Set.of(validIso, invalidIso))).thenReturn(List.of(validCountry));

    service.persistNewDestinations(providerName, input);

    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    assertEquals(1, captor.getValue().size()); // Only valid country processed
  }

  @Test
  void persistNewDestinations_WithNullCoordinates_SetsNullInEntity() {
    CommonDestination dto =
        new CommonDestination(
            "D123",
            List.of(new CommonDestination.Translation("en", "Test")),
            DestinationType.CITY,
            null, // Null coordinates
            providerName,
            "US");

    Country country = new Country("US");

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(anySet())).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, Map.of("US", List.of(dto)));

    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    assertNull(captor.getValue().get(0).getCenterCoordinates());
  }

  @Test
  void persistNewDestinations_WithMultipleTranslations_CreatesAll() {
    CommonDestination dto =
        new CommonDestination(
            "D123",
            List.of(
                new CommonDestination.Translation("en", "Singapore"),
                new CommonDestination.Translation("fr", "Singapour")),
            DestinationType.CITY,
            null,
            providerName,
            "US");

    Country country = new Country("US");

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(anySet())).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, Map.of("US", List.of(dto)));

    ArgumentCaptor<List<Destination>> captor = ArgumentCaptor.forClass(List.class);
    verify(destinationRepository).saveAll(captor.capture());
    Destination destination = captor.getValue().getFirst();
    assertEquals(Optional.of("Singapore"), destination.getTranslation(LanguageCode.EN));
    assertEquals(Optional.of("Singapour"), destination.getTranslation(LanguageCode.FR));
  }

  @Test
  void persistNewDestinations_WithNoValidTranslations_SkipsDestination() {
    // Create destination with only invalid language code translations
    CommonDestination dto =
        new CommonDestination(
            "D123",
            List.of(new CommonDestination.Translation("invalid1", "Test1")),
            DestinationType.CITY,
            null,
            providerName,
            "US");

    Country country = new Country("US");

    when(bookingProviderService.getBookingProviderByName(providerName)).thenReturn(Optional.of(provider));
    when(countryRepository.findByIso2CodeIn(anySet())).thenReturn(List.of(country));

    service.persistNewDestinations(providerName, Map.of("US", List.of(dto)));

    // Verify destinationRepository.saveAll wasn't called
    verify(destinationRepository, never()).saveAll(anyList());
  }

  private static CommonDestination mockRawDestinationDTO() {

    return new CommonDestination(
        "test-id",
        List.of(new CommonDestination.Translation("en", "Test")),
        DestinationType.CITY,
        null,
        BookingProviderName.VIATOR,
        "US");
  }
}
