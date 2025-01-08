package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.converter.ViatorDestinationToDestinationConverter;
import com.asialocalguide.gateway.core.domain.*;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.dto.ViatorDestinationDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DestinationPersistenceService {

  private final DestinationRepository destinationRepository;

  private final BookingProviderRepository bookingProviderRepository;

  private final ViatorDestinationToDestinationConverter viatorDestinationConverter;

  public DestinationPersistenceService(
      DestinationRepository destinationRepository,
      BookingProviderRepository bookingProviderRepository,
      ViatorDestinationToDestinationConverter viatorDestinationConverter) {
    this.destinationRepository = destinationRepository;
    this.bookingProviderRepository = bookingProviderRepository;
    this.viatorDestinationConverter = viatorDestinationConverter;
  }

  @Transactional
  public void buildAndSaveDestinationsFromViatorDtos(List<ViatorDestinationDTO> dtosToSave) {
    Map<Long, Destination> createdDestinations = new HashMap<>();

    BookingProvider viatorProvider =
        bookingProviderRepository
            .findByName("VIATOR")
            .orElseThrow(() -> new IllegalStateException("Viator BookingProvider not found"));

    List<Destination> destinationsToSave =
        dtosToSave.stream()
            .map(dto -> buildDestination(dto, viatorProvider, createdDestinations))
            .toList();

    destinationRepository.saveAll(destinationsToSave);
  }

  private Destination buildDestination(
      ViatorDestinationDTO dto,
      BookingProvider provider,
      Map<Long, Destination> createdDestinations) {

    Destination destination = viatorDestinationConverter.convert(dto);

    if (destination == null) {
      log.error("Failed to convert ViatorDestinationDTO to Destination: {}", dto);
      return null;
    }

    DestinationTranslation translation =
        new DestinationTranslation(dto.getLocaleCode(), dto.getName());
    destination.addTranslation(translation);

    DestinationProviderMapping mapping =
        DestinationProviderMapping.builder()
            .providerDestinationId(String.valueOf(dto.getDestinationId()))
            .provider(provider)
            .build();
    destination.addProviderMapping(mapping);

    Destination parentDestination = resolveParentDestination(dto, createdDestinations);
    destination.setParentDestination(parentDestination);

    createdDestinations.put(dto.getDestinationId(), destination);

    return destination;
  }

  private Destination resolveParentDestination(
      ViatorDestinationDTO dto, Map<Long, Destination> createdDestinations) {

    List<Long> lookupIds = dto.getLookupIds();

    for (Long parentId : lookupIds) {
      Destination parent = createdDestinations.get(parentId);
      if (parent != null && parent.getType() == DestinationType.COUNTRY) {
        return parent;
      }
    }
    return null;
  }
}
