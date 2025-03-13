package com.asialocalguide.gateway.viator.util;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonDuration;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonImage;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonPricing;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonReviews;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO.DurationDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO.PricingDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO.ReviewsDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ViatorActivityAdapter {

    private ViatorActivityAdapter() {
    }

    public static CommonActivity toCommon(ViatorActivityDTO viator) {
        return new CommonActivity(
                viator.title(),
                viator.description(),
                mapImages(viator.images()),
                mapReviews(viator.reviews()),
                mapDuration(viator.duration()),
                mapPricing(viator.pricing()),
                viator.productUrl(),
                mapTagsToCategories(viator.tags()),
                BookingProviderName.VIATOR,
                viator.productCode()
        );
    }

    private static List<CommonImage> mapImages(List<ViatorActivityDTO.ImageDTO> images) {
        return Optional.ofNullable(images)
                .orElse(Collections.emptyList())
                .stream()
                // Only mapping cover image
                .filter(ViatorActivityDTO.ImageDTO::isCover)
                .flatMap(img -> img.variants().stream())
                .map(variant -> new CommonImage(variant.height(), variant.width(), variant.url()))
                .toList();
    }

    private static CommonReviews mapReviews(ReviewsDTO reviews) {
        return new CommonReviews(
                reviews.combinedAverageRating(),
                reviews.totalReviews()
        );
    }

    private static CommonDuration mapDuration(DurationDTO duration) {
        if (duration.fixedDurationInMinutes() != null) {
            return new CommonDuration(
                    duration.fixedDurationInMinutes(),
                    duration.fixedDurationInMinutes()
            );
        }

        return new CommonDuration(
                duration.variableDurationFromMinutes(),
                duration.variableDurationToMinutes()
        );
    }

    private static CommonPricing mapPricing(PricingDTO pricing) {
        return new CommonPricing(
                Optional.ofNullable(pricing.summary())
                        .map(PricingDTO.SummaryDTO::fromPrice)
                        .orElse(null),
                pricing.currency()
        );
    }

    private static List<String> mapTagsToCategories(List<Integer> tags) {
        // Implement your tag ID to category name mapping logic here
        return Optional.ofNullable(tags)
                .orElse(Collections.emptyList())
                .stream()
                .map(String::valueOf) // Convert to string placeholder
                .toList();
    }

}