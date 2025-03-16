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
        if (viator == null) {
            return new CommonActivity(
                    "[DEFAULT] Missing Activity",
                    "",
                    Collections.emptyList(),
                    new CommonReviews(0.0, 0),
                    new CommonDuration(0, 0),
                    new CommonPricing(0.0, "EUR"),
                    "",
                    Collections.emptyList(),
                    BookingProviderName.VIATOR,
                    "DEFAULT_ID"
            );
        }

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
                .flatMap(img ->
                        Optional.ofNullable(img.variants()).orElse(List.of()).stream())
                .map(variant -> new CommonImage(variant.height(), variant.width(), variant.url()))
                .toList();
    }

    private static CommonReviews mapReviews(ReviewsDTO reviews) {
        if (reviews == null) {
            return new CommonReviews(0.0, 0);
        }

        return new CommonReviews(
                reviews.combinedAverageRating(),
                reviews.totalReviews()
        );
    }

    private static CommonDuration mapDuration(DurationDTO duration) {
        if (duration == null) {
            return new CommonDuration(0, 0);
        }

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
        if (pricing == null) {
            return new CommonPricing(0.0, "UNKNOWN");
        }

        return new CommonPricing(
                Optional.ofNullable(pricing.summary())
                        .map(PricingDTO.SummaryDTO::fromPrice)
                        .orElse(0.0),
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