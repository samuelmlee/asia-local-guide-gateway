package com.asialocalguide.gateway.core.dto.user;

import com.asialocalguide.gateway.core.domain.user.AuthProviderName;

public record CreateUserDTO(String providerUserId, AuthProviderName providerName, String email, String name) {}
