package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.dto.auth.EmailCheckDTO;
import com.asialocalguide.gateway.core.dto.auth.EmailCheckResultDTO;
import com.asialocalguide.gateway.core.exception.UserDeletionException;
import com.asialocalguide.gateway.core.service.auth.AuthProviderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

  @DeleteMapping("/users")
  public void deleteUser(@NotNull Authentication authentication) {
    String uid = authentication.getName();

    if (uid == null || uid.isBlank()) {
      throw new UserDeletionException(
          "Provider User ID to be deleted cannot be null or empty", UserDeletionException.Type.VALIDATION);
    }

    authProviderService.deleteProviderUser(uid);
  }
}
