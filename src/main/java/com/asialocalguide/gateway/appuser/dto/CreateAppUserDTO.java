package com.asialocalguide.gateway.appuser.dto;

import com.asialocalguide.gateway.appuser.domain.AuthProviderName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating a new application user.
 *
 * @param providerUserId the user identifier assigned by the authentication provider
 * @param providerName   the authentication provider that issued the user ID
 * @param email          the user's email address; must be a valid email format
 * @param name           the user's display name; optional
 */
public record CreateAppUserDTO(@NotEmpty String providerUserId, @NotNull AuthProviderName providerName,
		@NotBlank @Email String email, String name) {
}
