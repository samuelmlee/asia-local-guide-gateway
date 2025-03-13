package com.asialocalguide.gateway.core.domain.planning;

import java.time.LocalDate;
import java.util.List;

public record ProviderActivityData(
        List<CommonActivity> activities, ActivityData activityData, LocalDate startDate) {
}
