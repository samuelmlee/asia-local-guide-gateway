package com.asialocalguide.gateway.core.controller;

import com.asialocalguide.gateway.core.domain.user.User;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/create-user")
  public User createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
    return userService.createUser(createUserDTO);
  }
}
