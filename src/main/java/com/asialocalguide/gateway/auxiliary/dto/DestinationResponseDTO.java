package com.asialocalguide.gateway.auxiliary.dto;

import com.asialocalguide.gateway.auxiliary.domain.Destination;
import java.util.List;

public record DestinationResponseDTO(List<Destination> destinations, Integer totalCount) {}
