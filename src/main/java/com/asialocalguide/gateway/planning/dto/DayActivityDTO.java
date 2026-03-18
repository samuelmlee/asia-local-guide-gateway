package com.asialocalguide.gateway.planning.dto;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.CommonActivity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a single scheduled activity within a day plan, returned to the client.
 *
 * @param productCode            provider-specific activity identifier
 * @param title                  localized activity title
 * @param description            localized activity description
 * @param combinedAverageRating  aggregated average rating across providers
 * @param reviewCount            total number of reviews
 * @param durationMinutes        activity duration in minutes
 * @param fromPrice              starting price for the activity
 * @param currency               ISO 4217 currency code for the price
 * @param images                 list of cover images
 * @param providerUrl            direct booking URL
 * @param startTime              scheduled start time ({@code yyyy-MM-dd'T'HH:mm})
 * @param endTime                scheduled end time ({@code yyyy-MM-dd'T'HH:mm})
 * @param bookingProviderName    the provider supplying this activity
 */
public record DayActivityDTO(String productCode, String title, String description, Double combinedAverageRating,
		Integer reviewCount, Integer durationMinutes, Double fromPrice, String currency,
		List<CommonActivity.CommonImage> images, String providerUrl,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime,
		BookingProviderName bookingProviderName) {
}
