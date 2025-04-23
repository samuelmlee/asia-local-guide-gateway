package com.asialocalguide.gateway.core.domain.planning;

import java.time.LocalDate;
import java.util.List;

public record ProviderActivityPlanningData(
    List<CommonActivity> activities, ActivityPlanningData activityPlanningData, LocalDate startDate) {}
