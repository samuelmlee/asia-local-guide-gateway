package com.asialocalguide.gateway.planning.repository.custom;

import java.util.List;
import java.util.UUID;

import com.asialocalguide.gateway.core.domain.QLanguage;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.planning.domain.Planning;
import com.asialocalguide.gateway.planning.domain.QActivity;
import com.asialocalguide.gateway.planning.domain.QActivityTranslation;
import com.asialocalguide.gateway.planning.domain.QDayActivity;
import com.asialocalguide.gateway.planning.domain.QDayPlan;
import com.asialocalguide.gateway.planning.domain.QPlanning;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * QueryDSL implementation of {@link CustomPlanningRepository}.
 *
 * <p>{@code getPlanningsByAppUserIdAndLanguageCode} performs multiple fetch joins across
 * planning → day plan → day activity → activity → translation → language to load
 * all required associations in a single query.
 */
public class CustomPlanningRepositoryImpl implements CustomPlanningRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * @param queryFactory the QueryDSL JPA query factory
	 */
	public CustomPlanningRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean existsByAppUserIdAndName(UUID appUserId, String name) {
		QPlanning planning = QPlanning.planning;

		return queryFactory.selectOne()
				.from(planning)
				.where(planning.appUser.id.eq(appUserId).and(planning.name.eq(name)))
				.fetchFirst() != null;
	}

	/**
	 * {@inheritDoc}
	 */
	// TODO: Redo query to fetch data for Planning Summary in Frontend
	public List<Planning> getPlanningsByAppUserIdAndLanguageCode(UUID appUserId, LanguageCode languageCode) {
		QPlanning planning = QPlanning.planning;
		QDayPlan dayPlan = QDayPlan.dayPlan;
		QDayActivity dayActivity = QDayActivity.dayActivity;
		QActivity activity = QActivity.activity;
		QActivityTranslation activityTranslation = QActivityTranslation.activityTranslation;
		QLanguage language = QLanguage.language;

		return queryFactory.selectFrom(planning)
				.distinct()
				.join(planning.dayPlans, dayPlan)
				.fetchJoin()
				.join(dayPlan.dayActivities, dayActivity)
				.fetchJoin()
				.join(dayActivity.activity, activity)
				.fetchJoin()
				.join(activity.activityTranslations, activityTranslation)
				.fetchJoin()
				.join(activityTranslation.language, language)
				.fetchJoin()
				.where(planning.appUser.id.eq(appUserId).and(language.code.eq(languageCode)))
				.fetch();
	}
}
