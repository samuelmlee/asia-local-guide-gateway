package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.DestinationType;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import com.asialocalguide.gateway.core.service.composer.DestinationProvider;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import com.asialocalguide.gateway.viator.util.Iso2CodeLookupMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ViatorDestinationProvider implements DestinationProvider {

    private static final BookingProviderName PROVIDER_TYPE = BookingProviderName.VIATOR;

    private final ViatorClient viatorClient;

    public ViatorDestinationProvider(ViatorClient viatorClient) {
        this.viatorClient = viatorClient;
    }

    @Override
    public BookingProviderName getProviderName() {
        return PROVIDER_TYPE;
    }


    @Override
    public List<RawDestinationDTO> getDestinations() throws ViatorApiException {
        log.info(
                "Fetching Viator destinations for languages: {}", Arrays.toString(LanguageCode.values()));

        Map<LanguageCode, Map<Long, ViatorDestinationDTO>> languageToDestinations =
                new EnumMap<>(LanguageCode.class);

        List<CompletableFuture<List<ViatorDestinationDTO>>> futures = Arrays.stream(LanguageCode.values())
                .map(
                        language ->
                                CompletableFuture.supplyAsync(
                                                () -> {
                                                    List<ViatorDestinationDTO> destinations =
                                                            viatorClient.getAllDestinationsForLanguage(language.toString());

                                                    if (destinations == null || destinations.isEmpty()) {
                                                        throw new ViatorApiException(
                                                                String.format("No destinations found for language: %s aborting ingestion.", language));
                                                    }
                                                    return destinations;
                                                })
                                        .exceptionally(ex -> {
                                            throw new ViatorApiException(
                                                    String.format("API failure for getAllDestinationsForLanguage call for language: %s.", language), ex);
                                        })
                ).toList();

        List<List<ViatorDestinationDTO>> results = new ArrayList<>();
        for (CompletableFuture<List<ViatorDestinationDTO>> future : futures) {
            try {
                results.add(future.join());
            } catch (CompletionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ViatorApiException viatorApiException) {
                    throw viatorApiException;
                }
                throw new ViatorApiException("Error processing destinations", cause);
            }
        }

        // Construct the map of destinations by language
        for (int i = 0; i < LanguageCode.values().length; i++) {
            languageToDestinations.put(LanguageCode.values()[i], results.get(i).stream()
                    .collect(Collectors.toMap(ViatorDestinationDTO::destinationId, Function.identity())));
        }

        // Use English destinations as base for creating RawDestinationDTOs, other languages used
        // for translations
        Map<Long, ViatorDestinationDTO> idToDestinationEnDTOs =
                languageToDestinations.get(LanguageCode.EN);

        return idToDestinationEnDTOs.values().stream()
                // The app does not support scheduling activities within a whole country
                .filter(d -> d != null && !"COUNTRY".equals(d.type()) && d.lookupIds() != null)
                .map(dto -> createRawDestinationDTO(dto, languageToDestinations))
                .filter(Objects::nonNull)
                .toList();
    }

    private RawDestinationDTO createRawDestinationDTO(
            ViatorDestinationDTO dto,
            Map<LanguageCode, Map<Long, ViatorDestinationDTO>> languageToDestinations) {

        if (dto == null) {
            log.warn("Skipping null Destination in createRawDestinationDTO.");
            return null;
        }

        ViatorDestinationDTO country =
                resolveDestinationCountry(dto, languageToDestinations.get(LanguageCode.EN)).orElse(null);

        if (country == null) {
            log.warn("Skipping destination {} due to missing country.", dto);
            return null;
        }

        String countryIsoCode = Iso2CodeLookupMap.getIso2Code(country.name());

        if (countryIsoCode == null) {
            log.warn("Skipping destination {} due to no matching iso code for country.", country.name());
            return null;
        }

        return
                new RawDestinationDTO(
                        String.valueOf(dto.destinationId()),
                        resolveTranslations(dto.destinationId(), languageToDestinations),
                        mapToDestinationType(dto.type()),
                        dto.center(),
                        PROVIDER_TYPE,
                        countryIsoCode);
    }

    private Optional<ViatorDestinationDTO> resolveDestinationCountry(
            ViatorDestinationDTO dto, Map<Long, ViatorDestinationDTO> idToDestination) {

        if (dto == null || dto.lookupIds() == null) {
            return Optional.empty();
        }

        return dto.lookupIds().stream()
                .map(idToDestination::get)
                .filter(d -> d != null && "COUNTRY".equals(d.type()))
                .findFirst();
    }

    private List<RawDestinationDTO.Translation> resolveTranslations(
            Long destinationId,
            Map<LanguageCode, Map<Long, ViatorDestinationDTO>> languageToDestinations) {

        return languageToDestinations.entrySet().stream()
                .map(
                        entry -> {
                            Map<Long, ViatorDestinationDTO> idToDestination = entry.getValue();

                            if (idToDestination == null) {
                                log.debug(
                                        "No idToDestination Map found for language: {} while processing destinationId: {}",
                                        entry.getKey(),
                                        destinationId);
                                return null;
                            }

                            ViatorDestinationDTO dto = idToDestination.get(destinationId);
                            if (dto == null) {
                                log.debug(
                                        "No translation found for destinationId: {} in language: {}",
                                        destinationId,
                                        entry.getKey());
                                return null;
                            }
                            return new RawDestinationDTO.Translation(entry.getKey().toString(), dto.name());
                        })
                .filter(Objects::nonNull)
                .toList();
    }

    private DestinationType mapToDestinationType(String viatorType) {
        if (viatorType == null || viatorType.isBlank()) {
            return DestinationType.OTHER;
        }

        return switch (viatorType) {
            case "CITY", "TOWN", "VILLAGE" -> DestinationType.CITY;
            case "REGION",
                 "AREA",
                 "STATE",
                 "PROVINCE",
                 "COUNTY",
                 "HAMLET",
                 "ISLAND",
                 "NATIONAL_PARK",
                 "PENINSULA",
                 "UNION_TERRITORY" -> DestinationType.REGION;
            case "DISTRICT", "NEIGHBORHOOD", "WARD" -> DestinationType.DISTRICT;
            default -> DestinationType.OTHER;
        };
    }

}
