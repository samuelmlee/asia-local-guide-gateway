package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.auth.EmailCheckDTO;
import com.asialocalguide.gateway.core.dto.auth.EmailCheckResultDTO;
import com.asialocalguide.gateway.core.service.auth.AuthProviderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

  @DeleteMapping
  public void deleteUser(Authentication authentication) {
    String uid = authentication.getName();

    authProviderService.deleteUser(uid);
  }
}
