package com.asialocalguide.gateway.viator.dto;

import java.util.List;

/**
 * Viator API representation of a single activity tag.
 *
 * @param tagId            unique Viator tag identifier
 * @param parentTagIds     IDs of parent tags in the tag hierarchy
 * @param allNamesByLocale localized names for the tag
 */
public record ViatorActivityTagDTO(Long tagId, List<Long> parentTagIds, ViatorActivityTagName allNamesByLocale) {
}
