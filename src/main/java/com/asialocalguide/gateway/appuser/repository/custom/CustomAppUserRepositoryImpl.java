package com.asialocalguide.gateway.appuser.repository.custom;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.domain.AuthProviderName;
import com.asialocalguide.gateway.appuser.domain.QAppUser;
import com.asialocalguide.gateway.appuser.domain.QUserAuth;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class CustomAppUserRepositoryImpl implements CustomAppUserRepository {

	private final JPAQueryFactory queryFactory;

	public CustomAppUserRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AppUser> findUserByProviderNameAndProviderUserId(AuthProviderName providerName,
			String providerUserId) {

		QAppUser appUser = QAppUser.appUser;
		QUserAuth userAuth = QUserAuth.userAuth;

		return Optional.ofNullable(queryFactory.select(appUser)
				.from(appUser)
				.join(appUser.userAuths, userAuth)
				.fetchJoin()
				.where(userAuth.id.authProviderName.eq(providerName), userAuth.providerUserId.eq(providerUserId))
				.fetchOne());
	}
}
