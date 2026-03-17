package com.asialocalguide.gateway.core.repository.custom;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.activitytag.domain.QActivityTag;
import com.asialocalguide.gateway.activitytag.domain.QActivityTagTranslation;
import com.asialocalguide.gateway.core.domain.QLanguage;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class CustomActivityTagRepositoryImpl implements CustomActivityTagRepository {

	private final JPAQueryFactory queryFactory;

	public CustomActivityTagRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityTag> findAllWithTranslations(LanguageCode languageCode) {
		QActivityTag activityTag = QActivityTag.activityTag;
		QActivityTagTranslation translation = QActivityTagTranslation.activityTagTranslation;
		QLanguage language = QLanguage.language;

		return queryFactory.selectDistinct(activityTag)
				.from(activityTag)
				.leftJoin(activityTag.activityTagTranslations, translation)
				.fetchJoin()
				.leftJoin(translation.language, language)
				.fetchJoin()
				.where(translation.language.code.eq(languageCode))
				.fetch();
	}
}
