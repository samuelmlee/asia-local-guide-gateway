package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.domain.user.User;
import java.util.Optional;

public interface CustomUserRepository {

  Optional<User> findUserByProviderNameAndProviderUserId(AuthProviderName providerName, String providerUserId);
}
