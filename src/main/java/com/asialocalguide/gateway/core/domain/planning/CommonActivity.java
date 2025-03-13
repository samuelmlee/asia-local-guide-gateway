package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;

import java.util.List;

public record CommonActivity(

        String title,
        String description,
        List<CommonImageDTO> images,
        CommonReviewsDTO reviews,
        CommonDurationDTO duration,
        CommonPricingDTO pricing,
        String bookingUrl,
        List<String> categories,
        // To implement when distances between activities are needed
//        LocationDTO location,

        BookingProviderName providerName,
        String providerId
) {

    public record CommonImageDTO(Integer height, Integer width, String url) {
    }

    public record CommonReviewsDTO(Double averageRating, Integer totalReviews) {
    }

    public record CommonDurationDTO(Integer minMinutes, Integer maxMinutes) {
    }

    public record CommonPricingDTO(Double amount, String currency) {
    }


}
