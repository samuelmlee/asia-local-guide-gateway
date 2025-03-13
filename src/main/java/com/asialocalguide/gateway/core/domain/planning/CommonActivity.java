package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;

import java.util.List;

public record CommonActivity(

        String title,
        String description,
        List<CommonImage> images,
        CommonReviews reviews,
        CommonDuration duration,
        CommonPricing pricing,
        String bookingUrl,
        List<String> categories,
        // To implement when distances between activities are needed
//        LocationDTO location,

        BookingProviderName providerName,
        String providerId
) {

    public record CommonImage(Integer height, Integer width, String url) {
    }

    public record CommonReviews(Double averageRating, Integer totalReviews) {
    }

    public record CommonDuration(Integer minMinutes, Integer maxMinutes) {
    }

    public record CommonPricing(Double amount, String currency) {
    }


}
