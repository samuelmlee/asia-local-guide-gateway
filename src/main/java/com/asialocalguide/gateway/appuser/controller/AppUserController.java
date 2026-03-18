package com.asialocalguide.gateway.appuser.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.dto.AppUserDTO;
import com.asialocalguide.gateway.appuser.dto.CreateAppUserDTO;
import com.asialocalguide.gateway.appuser.service.AppUserService;

import jakarta.validation.Valid;

/**
 * REST controller for managing application users.
 *
 * <p>Provides endpoints to create and retrieve {@link AppUser} resources.
 */
@RestController
@RequestMapping("/v1/users")
public class AppUserController {

	private final AppUserService appUserService;

	/**
	 * @param appUserService service handling app user business logic
	 */
	public AppUserController(AppUserService appUserService) {

		this.appUserService = appUserService;
	}

	/**
	 * Creates a new application user from the given provider registration data.
	 *
	 * <p>Returns {@code 201 Created} with a {@code Location} header pointing to the new user resource.
	 *
	 * @param createAppUserDTO validated DTO containing provider user ID, provider name, email, and optional name
	 * @return response containing the created user's ID and its location URI
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<AppUserDTO> createAppUser(@RequestBody @Valid CreateAppUserDTO createAppUserDTO) {

		AppUser appUser = appUserService.createAppUser(createAppUserDTO);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(appUser.getId())
				.toUri();

		return ResponseEntity.created(location).body(new AppUserDTO(appUser.getId()));
	}

	/**
	 * Retrieves the application user with the given ID.
	 *
	 * @param id the UUID of the user to retrieve
	 * @return response containing the user's ID
	 */
	@GetMapping("/{id}")
	public ResponseEntity<AppUserDTO> getAppUser(@PathVariable UUID id) {
		AppUser appUser = appUserService.getAppUserById(id);
		return ResponseEntity.ok(new AppUserDTO(appUser.getId()));
	}
}
