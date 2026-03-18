package com.asialocalguide.gateway.viator.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Request body for the Viator product search endpoint.
 *
 * @param filtering  criteria to narrow the search (destination, tags, dates, rating)
 * @param sorting    result ordering
 * @param pagination page start index and page size
 * @param currency   the ISO 4217 currency code to use for pricing
 */
public record ViatorActivitySearchDTO(Filtering filtering, Sorting sorting, Pagination pagination, String currency) {

	/**
	 * @param destination Viator destination ID to search within
	 * @param tags        list of Viator tag IDs to filter by
	 * @param startDate   earliest activity start date
	 * @param endDate     latest activity end date
	 * @param rating      traveler rating range filter
	 */
	public record Filtering(Long destination, List<Integer> tags, LocalDate startDate, LocalDate endDate,
			Range rating) {
	}

	/**
	 * @param from inclusive lower bound of the range
	 * @param to   inclusive upper bound of the range
	 */
	public record Range(Integer from, Integer to) {
	}

	/**
	 * @param sort  the field to sort by
	 * @param order the sort direction
	 */
	public record Sorting(ViatorActivitySortingType sort, ViatorActivitySortingOrder order) {
	}

	/**
	 * @param start 1-based index of the first result
	 * @param count number of results per page
	 */
	public record Pagination(Integer start, Integer count) {
	}
}
