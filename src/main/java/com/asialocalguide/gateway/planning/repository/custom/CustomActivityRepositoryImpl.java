package com.asialocalguide.gateway.planning.repository.custom;

import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.planning.domain.Activity;
import com.asialocalguide.gateway.planning.domain.QActivity;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * QueryDSL implementation of {@link CustomActivityRepository}.
 */
public class CustomActivityRepositoryImpl implements CustomActivityRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * @param queryFactory the QueryDSL JPA query factory
	 */
	public CustomActivityRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Set<String> findExistingIdsByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
		QActivity activity = QActivity.activity;

		return Set.copyOf(queryFactory.select(activity.providerActivityId)
				.from(activity)
				.where(activity.provider.name.eq(providerName).and(activity.providerActivityId.in(activityIds)))
				.fetch());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Set<Activity> findActivitiesByProviderNameAndIds(BookingProviderName providerName, Set<String> activityIds) {
		QActivity activity = QActivity.activity;

		return Set.copyOf(queryFactory.select(activity)
				.from(activity)
				.where(activity.provider.name.eq(providerName).and(activity.providerActivityId.in(activityIds)))
				.fetch());
	}
}
