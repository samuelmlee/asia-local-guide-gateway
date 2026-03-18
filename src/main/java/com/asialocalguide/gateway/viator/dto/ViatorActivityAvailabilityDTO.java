package com.asialocalguide.gateway.viator.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Viator API response representing the availability schedule for a single product.
 *
 * @param productCode   the Viator product code
 * @param bookableItems one or more bookable option variants; must not be empty
 * @param currency      the currency used for pricing
 * @param summary       pricing summary (starting price)
 */
public record ViatorActivityAvailabilityDTO(@NotNull String productCode,
		@NotNull @NotEmpty List<BookableItem> bookableItems, String currency, Summary summary) {

	/**
	 * @param productOptionCode option code for this bookable variant
	 * @param seasons           seasonal availability windows; must not be empty
	 */
	public record BookableItem(String productOptionCode, @NotNull @NotEmpty List<Season> seasons) {
	}

	/**
	 * @param startDate      inclusive start of the seasonal window ({@code yyyy-MM-dd})
	 * @param endDate        inclusive end of the seasonal window, or blank if open-ended
	 * @param pricingRecords day-of-week and time-slot availability records; must not be empty
	 */
	public record Season(String startDate, String endDate, @NotNull @NotEmpty List<PricingRecord> pricingRecords) {
	}

	/**
	 * @param daysOfWeek  the days on which the pricing record applies (e.g. {@code "MONDAY"})
	 * @param timedEntries timed availability entries within the pricing record; must not be empty
	 */
	public record PricingRecord(@NotNull @NotEmpty List<String> daysOfWeek,
			@NotNull @NotEmpty List<TimedEntry> timedEntries) {
	}

	/**
	 * @param startTime        the start time of the entry in {@code HH:mm} format
	 * @param unavailableDates specific dates when this time entry is unavailable
	 */
	public record TimedEntry(String startTime, List<UnavailableDate> unavailableDates) {
	}

	/**
	 * @param date   the unavailable date in {@code yyyy-MM-dd} format
	 * @param reason the reason for unavailability
	 */
	public record UnavailableDate(String date, String reason) {
	}

	/**
	 * @param fromPrice the starting price for this product
	 */
	public record Summary(double fromPrice) {
	}
}
