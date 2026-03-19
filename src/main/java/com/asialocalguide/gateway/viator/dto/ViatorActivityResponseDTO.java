package com.asialocalguide.gateway.viator.dto;

import java.util.List;

/**
 * Viator API response for a product search, containing the matched products and total count.
 *
 * @param products   the list of activities matching the search criteria
 * @param totalCount total number of results across all pages
 */
public record ViatorActivityResponseDTO(List<ViatorActivityDTO> products, Integer totalCount) {
}
