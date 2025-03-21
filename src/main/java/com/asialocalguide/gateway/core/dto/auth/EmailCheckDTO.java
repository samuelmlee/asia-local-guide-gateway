package com.asialocalguide.gateway.core.dto.auth;

import jakarta.validation.constraints.Email;

public record EmailCheckDTO(@Email String email) {
}
