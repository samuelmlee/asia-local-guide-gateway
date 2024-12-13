package com.asialocalguide.gateway.auxiliary.service;

import com.asialocalguide.gateway.auxiliary.client.DestinationClient;
import com.asialocalguide.gateway.auxiliary.domain.Destination;
import com.asialocalguide.gateway.auxiliary.dto.DestinationDTO;
import com.asialocalguide.gateway.auxiliary.exception.DestinationApiException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DestinationService {

  @Autowired private DestinationClient destinationClient;

  public List<DestinationDTO> getAllDestinations() {
    List<Destination> destination = destinationClient.getAllDestinations();

    if (destination == null || destination.isEmpty()) {
      throw new DestinationApiException("No destinations returned from the API");
    }

    return destination.stream()
        .map(d -> DestinationDTO.of(d.getDestinationId(), d.getName(), d.getType()))
        .collect(Collectors.toList());
  }
}
