package com.asialocalguide.gateway.core.controller;

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

import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.dto.user.UserDTO;
import com.asialocalguide.gateway.core.service.appuser.AppUserService;

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
  public ResponseEntity<UserDTO> createAppUser(@RequestBody @Valid CreateUserDTO createUserDTO) {

    AppUser appUser = appUserService.createAppUser(createUserDTO);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appUser.getId()).toUri();

    return ResponseEntity.created(location).body(new UserDTO(appUser.getId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getAppUser(@PathVariable UUID id) {
    AppUser appUser = appUserService.getAppUserById(id);
    return ResponseEntity.ok(new UserDTO(appUser.getId()));
  }
}
