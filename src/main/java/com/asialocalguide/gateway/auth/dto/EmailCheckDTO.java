package com.asialocalguide.gateway.auth.dto;

import jakarta.validation.constraints.Email;

public record EmailCheckDTO(@Email String email) {
}
