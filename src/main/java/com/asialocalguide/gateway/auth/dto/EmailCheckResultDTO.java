package com.asialocalguide.gateway.auth.dto;

import jakarta.validation.constraints.Email;

public record EmailCheckResultDTO(@Email String email, boolean exists) {
}
