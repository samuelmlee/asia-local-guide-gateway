package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinationSortingServiceTest {

  @Mock private DestinationRepository destinationRepository;
  @Mock private CountryRepository countryRepository;
  @Mock private BookingProviderMappingRepository bookingProviderMappingRepository;
  @Mock private DestinationPersistenceService destinationPersistenceService;

  @InjectMocks private DestinationSortingService sortingService;

  private final BookingProviderName providerName = BookingProviderName.VIATOR;
  private final String supportedIsoCode = "us";
  private final String unsupportedIsoCode = "xx";
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
    existingDestination.setCountry(Country.builder().iso2Code("us").build());
  }

  @Test
  void triageRawDestinations_ShouldFilterExistingDestinations() {
    // Setup
    List<RawDestinationDTO> input = List.of(validRawDto);
    DestinationIngestionInput ingestionInput = new DestinationIngestionInput(providerName, input);

    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Set.of("D123")); // Mark as existing

    // Execute
    sortingService.triageRawDestinations(ingestionInput);

    // Verify
    verify(destinationPersistenceService, never()).persistNewDestinations(any(), any());
    verify(destinationPersistenceService, never()).persistExistingDestinations(any(), any());
  }

  @Test
  void triageRawDestinations_ShouldHandleExistingDestinations() {
    // Setup
    List<RawDestinationDTO> input = List.of(validRawDto);
    DestinationIngestionInput ingestionInput = new DestinationIngestionInput(providerName, input);

    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Collections.emptySet());
    when(countryRepository.findAllIso2Codes()).thenReturn(Set.of(supportedIsoCode));
    when(destinationRepository.findByIsoCodes(anySet())).thenReturn(List.of(existingDestination));

    // Execute
    sortingService.triageRawDestinations(ingestionInput);

    // Verify persistence calls
    ArgumentCaptor<Map<Long, RawDestinationDTO>> existingDestCaptor = ArgumentCaptor.forClass(Map.class);

    verify(destinationPersistenceService).persistExistingDestinations(eq(providerName), existingDestCaptor.capture());

    assertEquals(1, existingDestCaptor.getValue().size());
  }

  @Test
  void triageRawDestinations_ShouldSkipUnsupportedIsoCodes() {
    // Setup
    RawDestinationDTO unsupportedDto =
        new RawDestinationDTO(
            "D456",
            List.of(new RawDestinationDTO.Translation("en", "Test")),
            DestinationType.CITY,
            null,
            providerName,
            unsupportedIsoCode);
    DestinationIngestionInput ingestionInput = new DestinationIngestionInput(providerName, List.of(unsupportedDto));

    when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
        .thenReturn(Collections.emptySet());
    when(countryRepository.findAllIso2Codes()).thenReturn(Set.of(supportedIsoCode));

    // Execute
    sortingService.triageRawDestinations(ingestionInput);

    // Verify
    verify(destinationPersistenceService, never()).persistNewDestinations(any(), any());
    verify(destinationPersistenceService, never()).persistExistingDestinations(any(), any());
  }
}
