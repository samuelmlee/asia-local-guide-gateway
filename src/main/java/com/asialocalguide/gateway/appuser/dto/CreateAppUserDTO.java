package com.asialocalguide.gateway.appuser.dto;

import com.asialocalguide.gateway.appuser.domain.AuthProviderName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateAppUserDTO(@NotEmpty String providerUserId, @NotNull AuthProviderName providerName,
		@NotBlank @Email String email, String name) {
}
