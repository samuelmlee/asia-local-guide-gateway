package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorDestinationResponseDTO(
    List<ViatorDestinationDTO> viatorDestinationDTOS, Integer totalCount) {}
