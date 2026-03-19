package com.asialocalguide.gateway.appuser.repository.custom;

import java.util.Optional;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.domain.AuthProviderName;

/**
 * Custom repository interface for {@link AppUser} queries that require
 * joins across the {@link com.asialocalguide.gateway.appuser.domain.UserAuth} association.
 */
public interface CustomAppUserRepository {

	/**
	 * Returns the user whose {@link com.asialocalguide.gateway.appuser.domain.UserAuth} matches
	 * the given provider name and provider-assigned user ID.
	 *
	 * @param providerName   the authentication provider to filter by
	 * @param providerUserId the provider-assigned user identifier
	 * @return an Optional containing the matching user, or empty if not found
	 */
	Optional<AppUser> findUserByProviderNameAndProviderUserId(AuthProviderName providerName, String providerUserId);
}
