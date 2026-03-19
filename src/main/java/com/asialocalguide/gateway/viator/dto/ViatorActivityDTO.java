package com.asialocalguide.gateway.viator.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Viator API product summary returned from the product search endpoint.
 *
 * <p>Contains localized title/description, images, reviews, duration, pricing, and metadata.
 * Use {@link #getDurationMinutes()} to obtain the effective duration regardless of whether
 * the activity has a fixed or variable duration.
 */
public record ViatorActivityDTO(String productCode, String title, String description, List<ImageDTO> images,
		@NotNull ReviewsDTO reviews, @NotNull DurationDTO duration, String confirmationType, String itineraryType,
		PricingDTO pricing, String productUrl, List<DestinationDTO> destinations, List<Integer> tags,
		List<String> flags, TranslationInfoDTO translationInfo) {

	public record ImageDTO(String imageSource, String caption, boolean isCover, List<VariantDTO> variants) {
		public record VariantDTO(Integer height, Integer width, String url) {
		}
	}

	public record ReviewsDTO(List<SourceDTO> sources, @NotNull Integer totalReviews,
			@NotNull Double combinedAverageRating) {
		public record SourceDTO(String provider, Integer totalCount, Double averageRating) {
		}
	}

	public record DurationDTO(Integer variableDurationFromMinutes, Integer variableDurationToMinutes,
			Integer fixedDurationInMinutes) {
	}

	public record PricingDTO(SummaryDTO summary, String currency) {
		public record SummaryDTO(Double fromPrice, Double fromPriceBeforeDiscount) {
		}
	}

	public record DestinationDTO(String ref, Boolean primary) {
	}

	public record TranslationInfoDTO(Boolean containsMachineTranslatedText, String translationSource) {
	}

	/**
	 * Returns the effective duration in minutes, preferring the fixed duration over the
	 * variable maximum. Returns {@code 0} if neither is set.
	 *
	 * @return duration in minutes, or {@code 0} if unavailable
	 */
	public int getDurationMinutes() {

		DurationDTO duration = duration();

		if (duration.fixedDurationInMinutes != null) {
			return duration.fixedDurationInMinutes();
		}

		if (duration.variableDurationToMinutes() != null) {
			return duration.variableDurationToMinutes();
		}

		return 0;
	}
}
