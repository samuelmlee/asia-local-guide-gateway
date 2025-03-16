package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.*;
import com.asialocalguide.gateway.core.repository.BookingProviderMappingRepository;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.core.service.destination.DestinationPersistenceService;
import com.asialocalguide.gateway.core.service.destination.DestinationSortingService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DestinationSortingServiceTest {

    @Mock
    private DestinationRepository destinationRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private BookingProviderMappingRepository bookingProviderMappingRepository;
    @Mock
    private DestinationPersistenceService destinationPersistenceService;

    @InjectMocks
    private DestinationSortingService sortingService;

    private final BookingProviderName providerName = BookingProviderName.VIATOR;
    private final String supportedIsoCode = "US";
    private CommonDestination validRawDto;
    private Destination existingDestination;

    @BeforeEach
    void setUp() {
        validRawDto =
                new CommonDestination(
                        "D123",
                        List.of(new CommonDestination.Translation("en", "New York")),
                        DestinationType.CITY,
                        new Coordinates(40.7128, -74.0060),
                        providerName,
                        supportedIsoCode);

        existingDestination = new Destination();
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

        ArgumentCaptor<Map<String, List<CommonDestination>>> newDestCaptor = ArgumentCaptor.forClass(Map.class);
        verify(destinationPersistenceService).persistNewDestinations(eq(providerName), newDestCaptor.capture());

        assertEquals(1, newDestCaptor.getValue().get(supportedIsoCode).size());
        assertEquals(validRawDto, newDestCaptor.getValue().get(supportedIsoCode).getFirst());
    }

    @Test
    void triageRawDestinations_MixedNewAndExisting_PersistsBothTypes() {
        // Setup existing destination match
        CommonDestination existingRawDto = validRawDto;

        // Setup new destination with different coordinates
        CommonDestination newRawDto =
                new CommonDestination(
                        "D456",
                        List.of(new CommonDestination.Translation("en", "Los Angeles")),
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

        ArgumentCaptor<Map<Long, CommonDestination>> existingCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, List<CommonDestination>>> newCaptor = ArgumentCaptor.forClass(Map.class);

        verify(destinationPersistenceService).persistExistingDestinations(eq(providerName), existingCaptor.capture());
        verify(destinationPersistenceService).persistNewDestinations(eq(providerName), newCaptor.capture());

        assertEquals(1, existingCaptor.getValue().size()); // existingRawDto matches coordinates
        assertEquals(1, newCaptor.getValue().get(supportedIsoCode).size()); // newRawDto
    }

    @Test
    void triageRawDestinations_NullCoordinates_HandledAsNewDestination() {
        CommonDestination nullCoordsDto =
                new CommonDestination(
                        "D789",
                        List.of(new CommonDestination.Translation("en", "Test")),
                        DestinationType.CITY,
                        null,
                        providerName,
                        supportedIsoCode);

        when(bookingProviderMappingRepository.findProviderDestinationIdsByProviderName(providerName))
                .thenReturn(Collections.emptySet());
        when(countryRepository.findAllIso2Codes()).thenReturn(Set.of(supportedIsoCode));
        when(destinationRepository.findByIsoCodes(anySet())).thenReturn(List.of(existingDestination));

        sortingService.triageRawDestinations(new DestinationIngestionInput(providerName, List.of(nullCoordsDto)));

        ArgumentCaptor<Map<String, List<CommonDestination>>> newCaptor = ArgumentCaptor.forClass(Map.class);
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
