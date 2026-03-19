package com.asialocalguide.gateway.auth.dto;

import jakarta.validation.constraints.Email;

/**
 * Response payload for an email existence check.
 *
 * @param email  the email address that was checked
 * @param exists {@code true} if the email is already registered with the authentication provider
 */
public record EmailCheckResultDTO(@Email String email, boolean exists) {
}
