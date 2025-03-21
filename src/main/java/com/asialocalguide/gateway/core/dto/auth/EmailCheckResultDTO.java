package com.asialocalguide.gateway.core.dto.auth;

import jakarta.validation.constraints.Email;

public record EmailCheckResultDTO(@Email String email, boolean exists) {
}
