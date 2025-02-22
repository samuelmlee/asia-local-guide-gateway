package com.asialocalguide.gateway.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DestinationSortingServiceTest {

  @Mock private DestinationRepository destinationRepository;
  @Mock private CountryRepository countryRepository;
  @Mock private BookingProviderMappingRepository bookingProviderMappingRepository;
  @Mock private DestinationPersistenceService destinationPersistenceService;

  @InjectMocks private DestinationSortingService sortingService;

  private final BookingProviderName providerName = BookingProviderName.VIATOR;
  private final String supportedIsoCode = "US";
  private final String unsupportedIsoCode = "XX";
  private RawDestinationDTO validRawDto;
  private Destination existingDestination;

  @BeforeEach
  void setUp() {
    validRawDto =
        new RawDestinationDTO(
            "D123",
            List.of(new RawDestinationDTO.Translation("en", "New York")),
            DestinationType.CITY,
            new Coordinates(40.7128, -74.0060),
            providerName,
            supportedIsoCode);

    existingDestination = new Destination();
    existingDestination.setId(1L);
    existingDestination.setCenterCoordinates(new Coordinates(40.7128, -74.0060));
    existingDestination.setCountry(new Country(supportedIsoCode));
  }

  @Test
  void triageRawDestinations_NullInput_ThrowsException() {
    assertThrows(NullPointerException.class, () -> sortingService.triageRawDestinations(null));
  }

  @Test
  void triageRawDestinations_ValidNewDestination_PersistsNewDestination() {
    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Collections.emptySet());
    when(countryRepository.findAllIso2Codes()).thenReturn(Set.of(supportedIsoCode));
    when(destinationRepository.findByIsoCodes(anySet())).thenReturn(Collections.emptyList());

    sortingService.triageRawDestinations(new DestinationIngestionInput(providerName, List.of(validRawDto)));

    ArgumentCaptor<Map<String, List<RawDestinationDTO>>> newDestCaptor = ArgumentCaptor.forClass(Map.class);
    verify(destinationPersistenceService).persistNewDestinations(eq(providerName), newDestCaptor.capture());

    assertEquals(1, newDestCaptor.getValue().get(supportedIsoCode).size());
    assertEquals(validRawDto, newDestCaptor.getValue().get(supportedIsoCode).get(0));
  }

  @Test
  void triageRawDestinations_MixedNewAndExisting_PersistsBothTypes() {
    // Setup existing destination match
    RawDestinationDTO existingRawDto = validRawDto;

    // Setup new destination with different coordinates
    RawDestinationDTO newRawDto =
        new RawDestinationDTO(
            "D456",
            List.of(new RawDestinationDTO.Translation("en", "Los Angeles")),
            DestinationType.CITY,
            new Coordinates(34.0522, -118.2437),
            providerName,
            supportedIsoCode);

    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Set.of("D999")); // Different ID so not filtered
    when(countryRepository.findAllIso2Codes()).thenReturn(Set.of(supportedIsoCode));
    when(destinationRepository.findByIsoCodes(anySet())).thenReturn(List.of(existingDestination));

    sortingService.triageRawDestinations(
        new DestinationIngestionInput(providerName, List.of(existingRawDto, newRawDto)));

    ArgumentCaptor<Map<Long, RawDestinationDTO>> existingCaptor = ArgumentCaptor.forClass(Map.class);
    ArgumentCaptor<Map<String, List<RawDestinationDTO>>> newCaptor = ArgumentCaptor.forClass(Map.class);

    verify(destinationPersistenceService).persistExistingDestinations(eq(providerName), existingCaptor.capture());
    verify(destinationPersistenceService).persistNewDestinations(eq(providerName), newCaptor.capture());

    assertEquals(1, existingCaptor.getValue().size()); // existingRawDto matches coordinates
    assertEquals(1, newCaptor.getValue().get(supportedIsoCode).size()); // newRawDto
  }

  @Test
  void triageRawDestinations_NullCoordinates_HandledAsNewDestination() {
    RawDestinationDTO nullCoordsDto =
        new RawDestinationDTO(
            "D789",
            List.of(new RawDestinationDTO.Translation("en", "Test")),
            DestinationType.CITY,
            null,
            providerName,
            supportedIsoCode);

    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Collections.emptySet());
    when(countryRepository.findAllIso2Codes()).thenReturn(Set.of(supportedIsoCode));
    when(destinationRepository.findByIsoCodes(anySet())).thenReturn(List.of(existingDestination));

    sortingService.triageRawDestinations(new DestinationIngestionInput(providerName, List.of(nullCoordsDto)));

    ArgumentCaptor<Map<String, List<RawDestinationDTO>>> newCaptor = ArgumentCaptor.forClass(Map.class);
    verify(destinationPersistenceService).persistNewDestinations(eq(providerName), newCaptor.capture());

    assertEquals(1, newCaptor.getValue().get(supportedIsoCode).size());
  }

  @Test
  void triageRawDestinations_AllDestinationsFiltered_NoPersistenceCalls() {
    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Set.of(validRawDto.destinationId()));

    sortingService.triageRawDestinations(new DestinationIngestionInput(providerName, List.of(validRawDto)));

    verify(destinationPersistenceService, never()).persistNewDestinations(any(), any());
    verify(destinationPersistenceService, never()).persistExistingDestinations(any(), any());
  }

  @Test
  void triageRawDestinations_EmptySupportedIsos_NoProcessing() {
    when(countryRepository.findAllIso2Codes()).thenReturn(Collections.emptySet());

    sortingService.triageRawDestinations(new DestinationIngestionInput(providerName, List.of(validRawDto)));

    verify(destinationPersistenceService, never()).persistNewDestinations(any(), any());
    verify(destinationPersistenceService, never()).persistExistingDestinations(any(), any());
  }

  @Test
  void triageRawDestinations_NullProviderInput_ThrowsException() {
    DestinationIngestionInput input = new DestinationIngestionInput(null, List.of(validRawDto));
    assertThrows(NullPointerException.class, () -> sortingService.triageRawDestinations(input));
  }
}
