package com.asialocalguide.gateway.viator.util;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonDurationDTO;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonImageDTO;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonPricingDTO;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity.CommonReviewsDTO;
import com.asialocalguide.gateway.core.dto.planning.ImageDTO;
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

    private static List<CommonImageDTO> mapImages(List<ImageDTO> images) {
        return Optional.ofNullable(images)
                .orElse(Collections.emptyList())
                .stream()
                // Only mapping cover image
                .filter(ImageDTO::isCover)
                .flatMap(img -> img.variants().stream())
                .map(variant -> new CommonImageDTO(variant.height(), variant.width(), variant.url()))
                .toList();
    }

    private static CommonReviewsDTO mapReviews(ReviewsDTO reviews) {
        return new CommonReviewsDTO(
                reviews.combinedAverageRating(),
                reviews.totalReviews()
        );
    }

    private static CommonDurationDTO mapDuration(DurationDTO duration) {
        if (duration.fixedDurationInMinutes() != null) {
            return new CommonDurationDTO(
                    duration.fixedDurationInMinutes(),
                    duration.fixedDurationInMinutes()
            );
        }

        return new CommonDurationDTO(
                duration.variableDurationFromMinutes(),
                duration.variableDurationToMinutes()
        );
    }

    private static CommonPricingDTO mapPricing(PricingDTO pricing) {
        return new CommonPricingDTO(
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