package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.domain.auth.AuthProviderService;
import com.asialocalguide.gateway.core.dto.auth.EmailCheckDTO;
import com.asialocalguide.gateway.core.dto.auth.EmailCheckResultDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthProviderService authProviderService;

    public AuthController(AuthProviderService authProviderService) {
        this.authProviderService = authProviderService;
    }

    @PostMapping("/check-email")
    public EmailCheckResultDTO isExistingEmail(@RequestBody @Valid EmailCheckDTO emailCheckDTO) {
        boolean exists = authProviderService.checkExistingEmail(emailCheckDTO.email());

        return new EmailCheckResultDTO(emailCheckDTO.email(), exists);

    }

}
