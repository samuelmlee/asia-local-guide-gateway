package com.asialocalguide.gateway.activitytag.dto;

/**
 * Data transfer object representing an activity tag for API responses.
 *
 * @param id         the unique identifier of the activity tag
 * @param name       the localized display name of the tag
 * @param promptText the localized prompt text associated with the tag
 */
public record ActivityTagDTO(Long id, String name, String promptText) {
}
