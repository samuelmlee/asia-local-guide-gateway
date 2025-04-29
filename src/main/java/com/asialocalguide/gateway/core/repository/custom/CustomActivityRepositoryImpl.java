package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.domain.planning.QActivity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public class CustomActivityRepositoryImpl implements CustomActivityRepository {

  private final JPAQueryFactory queryFactory;

  public CustomActivityRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<String> findExistingIdsByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
    QActivity activity = QActivity.activity;

    return Set.copyOf(
        queryFactory
            .select(activity.id.providerActivityId)
            .from(activity)
            .where(activity.provider.name.eq(providerName).and(activity.id.providerActivityId.in(activityIds)))
            .fetch());
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Activity> findActivitiesByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
    QActivity activity = QActivity.activity;

    return Set.copyOf(
        queryFactory
            .select(activity)
            .from(activity)
            .where(activity.provider.name.eq(providerName).and(activity.id.providerActivityId.in(activityIds)))
            .fetch());
  }
}
