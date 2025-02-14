package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViatorDestinationProviderTest {

    @Mock
    private ViatorClient viatorClient;

    @InjectMocks
    private ViatorDestinationProvider destinationProvider;

    private final Coordinates testCoords = new Coordinates(1.234, 5.678);
    private final ViatorDestinationDTO countryDestination = new ViatorDestinationDTO(
            1L, "France", "COUNTRY", List.of(1L), "en", testCoords
    );
    private final ViatorDestinationDTO validDestination = new ViatorDestinationDTO(
            2L, "Paris", "CITY", List.of(1L), "en", testCoords
    );

    @Test
    void getDestinations_shouldReturnValidDestinations() throws ViatorApiException {
        // Mock client responses
        when(viatorClient.getAllDestinationsForLanguage("en"))
                .thenReturn(List.of(countryDestination, validDestination));
        when(viatorClient.getAllDestinationsForLanguage("fr"))
                .thenReturn(List.of(
                        new ViatorDestinationDTO(1L, "France", "COUNTRY", List.of(1L), "fr", testCoords),
                        new ViatorDestinationDTO(2L, "Paris", "CITY", List.of(1L), "fr", testCoords)
                ));

        List<RawDestinationDTO> result = destinationProvider.getDestinations();

        assertThat(result).hasSize(1);
        RawDestinationDTO dto = result.getFirst();
        assertThat(dto.destinationId()).isEqualTo("2");
        assertThat(dto.type()).isEqualTo(DestinationType.CITY);
        assertThat(dto.countryIsoCode()).isEqualTo("fr");
        assertThat(dto.names())
                .extracting(RawDestinationDTO.Translation::languageCode)
                .containsExactlyInAnyOrder("en", "fr");
    }

    @Test
    void getDestinations_shouldFilterInvalidDestinations() throws ViatorApiException {
        ViatorDestinationDTO invalidType = new ViatorDestinationDTO(
                3L, "Country", "COUNTRY", List.of(1L), "en", testCoords
        );
        ViatorDestinationDTO nullLookupIds = new ViatorDestinationDTO(
                4L, "NullLookup", "CITY", null, "en", testCoords
        );

        when(viatorClient.getAllDestinationsForLanguage(anyString()))
                .thenReturn(List.of(countryDestination, invalidType, nullLookupIds));

        List<RawDestinationDTO> result = destinationProvider.getDestinations();
        assertThat(result).isEmpty();
    }

    @Test
    void getDestinations_shouldThrowWhenLanguageFails() {
        when(viatorClient.getAllDestinationsForLanguage("en"))
                .thenThrow(new ViatorApiException("API failure"));

        assertThatThrownBy(() -> destinationProvider.getDestinations())
                .isInstanceOf(ViatorApiException.class)
                .hasMessageContaining("API failure");
    }

    @Test
    void getDestinations_shouldHandleMissingCountry() throws ViatorApiException {
        ViatorDestinationDTO missingCountryDest = new ViatorDestinationDTO(
                5L, "NoCountry", "CITY", List.of(999L), "en", testCoords
        );

        // Mock valid responses for all required languages
        when(viatorClient.getAllDestinationsForLanguage("en"))
                .thenReturn(List.of(missingCountryDest));
        when(viatorClient.getAllDestinationsForLanguage("fr"))
                .thenReturn(List.of(
                        new ViatorDestinationDTO(5L, "Paris", "CITY", List.of(999L), "fr", testCoords)
                ));

        List<RawDestinationDTO> result = destinationProvider.getDestinations();
        assertThat(result).isEmpty();
    }


    @Test
    void getProviderName_shouldReturnViator() {
        assertThat(destinationProvider.getProviderName())
                .isEqualTo(BookingProviderName.VIATOR);
    }

    @Test
    void resolveTranslations_shouldHandleMissingTranslations() throws ViatorApiException {
        when(viatorClient.getAllDestinationsForLanguage("en"))
                .thenReturn(List.of(countryDestination, validDestination));
        when(viatorClient.getAllDestinationsForLanguage("fr"))
                .thenReturn(List.of(countryDestination)); // Missing French translation for Paris

        List<RawDestinationDTO> result = destinationProvider.getDestinations();
        assertThat(result.get(0).names())
                .extracting(RawDestinationDTO.Translation::languageCode)
                .containsExactly("en");
    }

}