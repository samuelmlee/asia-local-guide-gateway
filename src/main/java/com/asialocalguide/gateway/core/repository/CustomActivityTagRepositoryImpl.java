package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.activitytag.ActivityTag;
import com.asialocalguide.gateway.core.domain.activitytag.QActivityTag;
import com.asialocalguide.gateway.core.domain.activitytag.QActivityTagTranslation;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return queryFactory
                .selectDistinct(activityTag)
                .from(activityTag)
                .leftJoin(activityTag.activityTagTranslations, translation).fetchJoin()
                .where(translation.id.languageCode.eq(languageCode))
                .fetch();
    }
}
