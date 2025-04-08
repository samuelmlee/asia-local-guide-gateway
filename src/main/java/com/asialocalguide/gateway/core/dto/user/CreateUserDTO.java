package com.asialocalguide.gateway.core.dto.user;

import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateUserDTO(
    @NotEmpty String providerUserId, @NotNull AuthProviderName providerName, @NotEmpty String email, String name) {}
