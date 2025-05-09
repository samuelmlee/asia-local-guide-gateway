package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.dto.user.UserDTO;
import com.asialocalguide.gateway.core.service.appuser.AppUserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final AppUserService appUserService;

  public UserController(AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserDTO> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {

    AppUser appUser = appUserService.createAppUser(createUserDTO);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appUser.getId()).toUri();

    return ResponseEntity.created(location).body(new UserDTO(appUser.getId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
    AppUser appUser = appUserService.getAppUserById(id);
    return ResponseEntity.ok(new UserDTO(appUser.getId()));
  }
}
