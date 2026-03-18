package com.asialocalguide.gateway.planning.domain;

import com.asialocalguide.gateway.core.domain.BookingProviderName;

import java.util.List;

/**
 * Provider-agnostic representation of a bookable activity returned by a booking provider.
 *
 * @param title         localized title of the activity
 * @param description   localized description, may be {@code null}
 * @param images        available cover images
 * @param reviews       aggregated review data
 * @param duration      minimum and maximum duration in minutes
 * @param pricing       starting price and currency
 * @param bookingUrl    direct URL to book the activity
 * @param categories    list of activity category tags
 * @param providerName  the booking provider that supplies this activity
 * @param providerId    the provider's unique identifier for this activity
 */
public record CommonActivity(

		String title, String description, List<CommonImage> images, CommonReviews reviews, CommonDuration duration,
		CommonPricing pricing, String bookingUrl, List<String> categories,
		// To implement when distances between activities are needed
//        LocationDTO location,

		BookingProviderName providerName, String providerId) {

	/**
	 * @param height pixel height of the image
	 * @param width  pixel width of the image
	 * @param url    publicly accessible image URL
	 */
	public record CommonImage(Integer height, Integer width, String url) {
	}

	/**
	 * @param averageRating weighted average rating across all reviews
	 * @param totalReviews  total number of reviews
	 */
	public record CommonReviews(Double averageRating, Integer totalReviews) {
	}

	/**
	 * @param minMinutes minimum duration in minutes
	 * @param maxMinutes maximum duration in minutes
	 */
	public record CommonDuration(Integer minMinutes, Integer maxMinutes) {
	}

	/**
	 * @param amount   starting price amount
	 * @param currency ISO 4217 currency code
	 */
	public record CommonPricing(Double amount, String currency) {
	}

}
