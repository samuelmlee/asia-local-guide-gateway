package com.asialocalguide.gateway.auth.dto;

import jakarta.validation.constraints.Email;

/**
 * Request payload for checking whether an email address is already registered.
 *
 * @param email the email address to check; must be a valid email format
 */
public record EmailCheckDTO(@Email String email) {
}
