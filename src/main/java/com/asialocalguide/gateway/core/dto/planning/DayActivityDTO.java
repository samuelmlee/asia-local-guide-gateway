package com.asialocalguide.gateway.core.dto.planning;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.CommonActivity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record DayActivityDTO(
        String productCode,
        String title,
        String description,
        Double combinedAverageRating,
        Integer reviewCount,
        Integer durationMinutes,
        Double fromPrice,
        String currency,
        List<CommonActivity.CommonImage> images,
        String providerUrl,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime startTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime endTime,
        BookingProviderName bookingProviderName) {
}
