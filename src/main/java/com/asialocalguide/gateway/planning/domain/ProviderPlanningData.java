package com.asialocalguide.gateway.planning.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider-supplied planning data aggregating activities and their scheduling matrix.
 *
 * @param activities            list of raw activities returned by the provider
 * @param activityPlanningData  availability matrix and metadata used by the scheduler
 * @param startDate             first day of the requested planning period
 */
public record ProviderPlanningData(List<CommonActivity> activities, ActivityPlanningData activityPlanningData,
		LocalDate startDate) {
}
