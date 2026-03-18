package com.asialocalguide.gateway.appuser.dto;

import com.asialocalguide.gateway.appuser.domain.AuthProviderName;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateAppUserDTO(@NotEmpty String providerUserId, @NotNull AuthProviderName providerName,
		@NotEmpty String email, String name) {
}
