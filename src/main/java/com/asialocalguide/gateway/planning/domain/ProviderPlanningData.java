package com.asialocalguide.gateway.planning.domain;

import java.time.LocalDate;
import java.util.List;

public record ProviderPlanningData(List<CommonActivity> activities, ActivityPlanningData activityPlanningData,
		LocalDate startDate) {
}
