package com.asialocalguide.gateway.auth.controller;

import com.asialocalguide.gateway.auth.dto.EmailCheckDTO;
import com.asialocalguide.gateway.auth.dto.EmailCheckResultDTO;
import com.asialocalguide.gateway.auth.exception.ProviderUserDeletionException;
import com.asialocalguide.gateway.auth.service.AuthProviderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication-related operations.
 *
 * <p>Provides endpoints for checking email existence and deleting provider users.
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

	private final AuthProviderService authProviderService;

	/**
	 * @param authProviderService service delegating to the configured authentication provider
	 */
	public AuthController(AuthProviderService authProviderService) {
		this.authProviderService = authProviderService;
	}

	/**
	 * Checks whether the given email address is already registered with the authentication provider.
	 *
	 * @param emailCheckDTO validated DTO containing the email to check
	 * @return a result DTO with the email and a boolean indicating whether it exists
	 */
	@PostMapping("/check-email")
	public EmailCheckResultDTO isExistingEmail(@RequestBody @Valid EmailCheckDTO emailCheckDTO) {
		boolean exists = authProviderService.checkExistingEmail(emailCheckDTO.email());

		return new EmailCheckResultDTO(emailCheckDTO.email(), exists);
	}

	/**
	 * Deletes the authenticated user from the authentication provider.
	 *
	 * <p>The user ID is derived from the authenticated principal's name claim.
	 *
	 * @param authentication the current authentication token; must not be {@code null}
	 * @throws ProviderUserDeletionException if the user ID is missing or blank
	 */
	@DeleteMapping("/users")
	public void deleteProviderUser(@NotNull Authentication authentication) {
		String uid = authentication.getName();

		if (uid == null || uid.isBlank()) {
			throw new ProviderUserDeletionException("Provider User ID to be deleted cannot be null or empty",
					ProviderUserDeletionException.Type.VALIDATION);
		}

		authProviderService.deleteProviderUser(uid);
	}
}
