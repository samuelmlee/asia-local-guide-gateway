package com.asialocalguide.gateway.viator.dto;

import java.util.List;

/**
 * Viator API response containing all destinations for a given language.
 *
 * @param destinations the list of destinations returned by the API
 * @param totalCount   total number of destinations across all pages
 */
public record ViatorDestinationResponseDTO(List<ViatorDestinationDTO> destinations, Integer totalCount) {
}
