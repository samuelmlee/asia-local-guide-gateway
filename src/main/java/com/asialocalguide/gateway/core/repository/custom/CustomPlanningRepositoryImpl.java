package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.planning.QPlanning;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.UUID;

public class CustomPlanningRepositoryImpl implements CustomPlanningRepository {

  private final JPAQueryFactory queryFactory;

  public CustomPlanningRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public boolean existsByAppUserIdAndName(UUID appUserId, String name) {
    QPlanning planning = QPlanning.planning;

    return queryFactory
            .selectOne()
            .from(planning)
            .where(planning.appUser.id.eq(appUserId).and(planning.name.eq(name)))
            .fetchFirst()
        != null;
  }
}
