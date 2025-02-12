package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViatorDestinationProviderTest {

    @Mock
    private ViatorClient viatorClient;

    @InjectMocks
    private ViatorDestinationProvider viatorDestinationProvider;

    private ViatorDestinationDTO sampleDestination;

    @BeforeEach
    void setUp() {
        sampleDestination = new ViatorDestinationDTO(
                1L, // destinationId
                "CITY", // type
                "New York", // name
                List.of(100L), // lookupIds (e.g., country reference)
                "en",
                new Coordinates(40.7128, -74.0060)// center
        );

    }

    @Test
    void testGetProviderName() {
        assertEquals(BookingProviderName.VIATOR, viatorDestinationProvider.getProviderName());
    }

    @Test
    void testGetDestinations_SuccessfulFetch() {
        when(viatorClient.getAllDestinationsForLanguage(LanguageCode.EN.toString()))
                .thenReturn(List.of(sampleDestination));

        when(viatorClient.getAllDestinationsForLanguage(LanguageCode.FR.toString()))
                .thenReturn(Collections.emptyList());

        List<RawDestinationDTO> result = viatorDestinationProvider.getDestinations();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("1", result.getFirst().destinationId());
        verify(viatorClient, times(LanguageCode.values().length)).getAllDestinationsForLanguage(anyString());
    }

    @Test
    void testGetDestinations_EmptyResponse() {
        when(viatorClient.getAllDestinationsForLanguage(anyString()))
                .thenReturn(Collections.emptyList());

        List<RawDestinationDTO> result = viatorDestinationProvider.getDestinations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDestinations_NullResponse() {
        when(viatorClient.getAllDestinationsForLanguage(anyString()))
                .thenReturn(null);

        List<RawDestinationDTO> result = viatorDestinationProvider.getDestinations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDestinations_ApiExceptionHandling() {
        when(viatorClient.getAllDestinationsForLanguage(anyString()))
                .thenThrow(new ViatorApiException("API Error"));

        assertDoesNotThrow(() -> viatorDestinationProvider.getDestinations());
        verify(viatorClient, times(LanguageCode.values().length)).getAllDestinationsForLanguage(anyString());
    }


}