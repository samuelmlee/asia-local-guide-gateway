package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.Destination;
import com.asialocalguide.gateway.core.dto.DestinationDTO;
import com.asialocalguide.gateway.core.exception.DestinationRepositoryException;
import com.asialocalguide.gateway.core.repository.DestinationRepository;
import com.asialocalguide.gateway.viator.service.ViatorDestinationService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DestinationService {

  private final ViatorDestinationService viatorDestinationService;

  private final DestinationRepository destinationRepository;

  public DestinationService(
      ViatorDestinationService viatorDestinationService,
      DestinationRepository destinationRepository) {

    this.viatorDestinationService = viatorDestinationService;
    this.destinationRepository = destinationRepository;
  }

  public void syncViatorDestinations() {

    List<Destination> destinations = viatorDestinationService.getAllDestinations();

    destinations.stream()
        .filter(
            d -> {
              return !destinationRepository.existsByName(d.getName());
            })
        .forEach(destinationRepository::save);
  }

  public List<DestinationDTO> getAllDestinations() {
    List<Destination> destinations = destinationRepository.findAll();

    if (destinations.isEmpty()) {
      throw new DestinationRepositoryException("No destinations returned from the Repository");
    }

    return destinations.stream()
        .map(d -> DestinationDTO.of(d.getId(), d.getName(), d.getType()))
        .collect(Collectors.toList());
  }
}
