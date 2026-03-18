package com.asialocalguide.gateway.appuser.repository.custom;

import java.util.Optional;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.domain.AuthProviderName;

public interface CustomAppUserRepository {

	Optional<AppUser> findUserByProviderNameAndProviderUserId(AuthProviderName providerName, String providerUserId);
}
