package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.auth.EmailCheckDTO;
import com.asialocalguide.gateway.core.dto.auth.EmailCheckResultDTO;
import com.asialocalguide.gateway.core.service.auth.AuthProviderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

  @DeleteMapping("/{uid}")
  public void deleteUser(@PathVariable("uid") @NotBlank String uid) {
    authProviderService.deleteUser(uid);
  }
}
