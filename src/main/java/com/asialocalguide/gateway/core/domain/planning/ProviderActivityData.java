package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import java.time.LocalDate;
import java.util.List;

public record ProviderActivityData(
    List<ViatorActivityDTO> activities, ActivityData activityData, LocalDate startDate) {}
