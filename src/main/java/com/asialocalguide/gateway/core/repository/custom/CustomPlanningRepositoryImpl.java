package com.asialocalguide.gateway.core.repository.custom;

import java.util.List;
import java.util.UUID;

import com.asialocalguide.gateway.core.domain.QLanguage;
import com.asialocalguide.gateway.core.domain.planning.Planning;
import com.asialocalguide.gateway.core.domain.planning.QActivity;
import com.asialocalguide.gateway.core.domain.planning.QActivityTranslation;
import com.asialocalguide.gateway.core.domain.planning.QDayActivity;
import com.asialocalguide.gateway.core.domain.planning.QDayPlan;
import com.asialocalguide.gateway.core.domain.planning.QPlanning;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class CustomPlanningRepositoryImpl implements CustomPlanningRepository {

	private final JPAQueryFactory queryFactory;

	public CustomPlanningRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public boolean existsByAppUserIdAndName(UUID appUserId, String name) {
		QPlanning planning = QPlanning.planning;

		return queryFactory.selectOne()
				.from(planning)
				.where(planning.appUser.id.eq(appUserId).and(planning.name.eq(name)))
				.fetchFirst() != null;
	}

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
