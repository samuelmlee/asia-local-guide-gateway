package com.asialocalguide.gateway.viator.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ViatorActivityAvailabilityDTO(
        @NotNull String productCode,
        @NotNull @NotEmpty List<BookableItem> bookableItems,
        String currency,
        Summary summary) {
    public record BookableItem(String productOptionCode, @NotNull @NotEmpty List<Season> seasons) {
    }

    public record Season(
            String startDate, String endDate, @NotNull @NotEmpty List<PricingRecord> pricingRecords) {
    }

    public record PricingRecord(
            List<String> daysOfWeek, @NotNull @NotEmpty List<TimedEntry> timedEntries) {
    }

    public record TimedEntry(String startTime, List<UnavailableDate> unavailableDates) {
    }

    public record UnavailableDate(String date, String reason) {
    }

    public record Summary(double fromPrice) {
    }
}
