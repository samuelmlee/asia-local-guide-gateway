package com.asialocalguide.gateway.viator.dto;

import java.util.List;

/**
 * Viator API response containing all available activity tags.
 *
 * @param tags the list of activity tags returned by the API
 */
public record ViatorActivityTagResponseDTO(List<ViatorActivityTagDTO> tags) {
}
