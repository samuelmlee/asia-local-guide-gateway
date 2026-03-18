package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Provider-agnostic representation of a destination fetched from an external booking provider.
 *
 * <p>Used as an intermediate data object during destination ingestion before entities are
 * persisted to the database.
 *
 * @param destinationId     the provider-assigned destination identifier
 * @param names             the localized names for the destination; must not be empty
 * @param type              the classification of the destination (city, region, etc.)
 * @param centerCoordinates the geographic center of the destination; may be {@code null}
 * @param providerName      the booking provider that supplied this destination
 * @param countryIsoCode    the ISO 3166-1 alpha-2 country code the destination belongs to
 */
public record CommonDestination(@NotNull String destinationId, @NotNull @NotEmpty List<Translation> names,
		@NotNull DestinationType type, Coordinates centerCoordinates, @NotNull BookingProviderName providerName,
		@NotNull String countryIsoCode) {

	/**
	 * A localized name for a destination in a specific language.
	 *
	 * @param languageCode the language of the name
	 * @param name         the localized display name
	 */
	public record Translation(@NotNull LanguageCode languageCode, @NotNull String name) {
	}
}
