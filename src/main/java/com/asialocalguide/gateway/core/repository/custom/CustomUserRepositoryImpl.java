package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.domain.user.QUser;
import com.asialocalguide.gateway.core.domain.user.QUserAuth;
import com.asialocalguide.gateway.core.domain.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class CustomUserRepositoryImpl implements CustomUserRepository {

  private final JPAQueryFactory queryFactory;

  public CustomUserRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findUserByProviderNameAndProviderUserId(AuthProviderName providerName, String providerUserId) {

    QUser user = QUser.user;
    QUserAuth userAuth = QUserAuth.userAuth;

    return Optional.ofNullable(
        queryFactory
            .select(user)
            .from(user)
            .join(user.userAuths, userAuth)
            .fetchJoin()
            .where(userAuth.id.authProviderName.eq(providerName), userAuth.providerUserId.eq(providerUserId))
            .fetchOne());
  }
}
