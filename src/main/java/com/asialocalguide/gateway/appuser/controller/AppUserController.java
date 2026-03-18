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

@RestController
@RequestMapping("/v1/users")
public class AppUserController {

	private final AppUserService appUserService;

	public AppUserController(AppUserService appUserService) {

		this.appUserService = appUserService;
	}

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

	@GetMapping("/{id}")
	public ResponseEntity<AppUserDTO> getAppUser(@PathVariable UUID id) {
		AppUser appUser = appUserService.getAppUserById(id);
		return ResponseEntity.ok(new AppUserDTO(appUser.getId()));
	}
}
