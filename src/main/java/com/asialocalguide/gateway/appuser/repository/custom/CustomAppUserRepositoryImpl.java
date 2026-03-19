package com.asialocalguide.gateway.appuser.repository.custom;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.domain.AuthProviderName;
import com.asialocalguide.gateway.appuser.domain.QAppUser;
import com.asialocalguide.gateway.appuser.domain.QUserAuth;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * QueryDSL-based implementation of {@link CustomAppUserRepository}.
 *
 * <p>Joins the {@code UserAuth} association to locate users by their external provider identity,
 * fetching the auth collection eagerly to prevent lazy-loading issues in callers.
 */
public class CustomAppUserRepositoryImpl implements CustomAppUserRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * @param queryFactory the QueryDSL factory used to build and execute JPQL queries
	 */
	public CustomAppUserRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Uses a fetch join on filtered {@code userAuths} so the collection is populated
	 * without additional queries after this method returns.
	 */
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
