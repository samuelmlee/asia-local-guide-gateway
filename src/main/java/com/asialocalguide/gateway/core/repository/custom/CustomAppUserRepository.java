package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import java.util.Optional;

public interface CustomAppUserRepository {

  Optional<AppUser> findUserByProviderNameAndProviderUserId(AuthProviderName providerName, String providerUserId);
}
