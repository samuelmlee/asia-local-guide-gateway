package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.domain.user.User;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.dto.user.UserDTO;
import com.asialocalguide.gateway.core.service.user.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserDTO> createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {

    User user = userService.createUser(createUserDTO);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();

    return ResponseEntity.created(location).body(new UserDTO(user.getId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
    User user = userService.getUserById(id);
    return ResponseEntity.ok(new UserDTO(user.getId()));
  }
}
