package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorActivityResponseDTO(List<ViatorActivityDTO> products, Integer totalCount) {}
