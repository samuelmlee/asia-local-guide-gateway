package com.asialocalguide.gateway.activitytag.repository.custom;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.activitytag.domain.QActivityTag;
import com.asialocalguide.gateway.activitytag.domain.QActivityTagTranslation;
import com.asialocalguide.gateway.core.domain.QLanguage;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * QueryDSL-based implementation of {@link CustomActivityTagRepository}.
 *
 * <p>Uses fetch joins to eagerly load translations and their languages in a single query,
 * avoiding N+1 select issues.
 */
@Repository
public class CustomActivityTagRepositoryImpl implements CustomActivityTagRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * @param queryFactory the QueryDSL factory used to build and execute JPQL queries
	 */
	public CustomActivityTagRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Performs a single query with fetch joins on translations and languages to avoid
	 * lazy loading during result mapping.
	 */
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
