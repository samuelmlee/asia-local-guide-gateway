package com.asialocalguide.gateway.planning.domain;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

import jakarta.validation.constraints.*;
import java.util.List;
import org.hibernate.validator.constraints.URL;

/**
 * Validated, provider-agnostic representation of an activity ready for persistence.
 *
 * <p>Used as the common contract between provider-specific fetch strategies and
 * {@link com.asialocalguide.gateway.planning.service.ActivityService}.
 *
 * @param title             non-empty list of localized titles; must not be {@code null}
 * @param description       non-empty list of localized descriptions; must not be {@code null}
 * @param images            optional list of cover images
 * @param review            aggregated review data; must not be {@code null}
 * @param durationInMinutes activity duration in minutes; must be positive
 * @param providerUrl       direct booking URL; must be a valid URL
 * @param providerName      the booking provider; must not be {@code null}
 * @param providerId        the provider's unique identifier; must not be {@code null}
 */
public record CommonPersistableActivity(@NotNull @NotEmpty List<Translation> title,
		@NotNull @NotEmpty List<Translation> description, List<Image> images, @NotNull Review review,
		@NotNull @Positive Integer durationInMinutes, @URL String providerUrl,
		@NotNull BookingProviderName providerName, @NotNull String providerId) {

	/**
	 * @param type   display context (desktop or mobile); must not be {@code null}
	 * @param height pixel height; must be positive
	 * @param width  pixel width; must be positive
	 * @param url    image URL; must not be blank
	 */
	public record Image(@NotNull ImageType type, @NotNull @Positive Integer height, @NotNull @Positive Integer width,
			@NotBlank String url) {
	}

	/**
	 * @param averageRating weighted average rating in the range [0.0, 5.0]; must not be {@code null}
	 * @param reviewCount   total review count; must be positive
	 */
	public record Review(@NotNull @DecimalMin("0.0") @DecimalMax("5.0") float averageRating,
			@NotNull @Positive Integer reviewCount) {
	}

	/**
	 * @param languageCode the language of this translation; must not be {@code null}
	 * @param value        the translated text; must not be blank
	 */
	public record Translation(@NotNull LanguageCode languageCode, @NotBlank String value) {
	}
}
