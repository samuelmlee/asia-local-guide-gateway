package com.asialocalguide.gateway.core.service.user;

import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  public void createUser(@Valid CreateUserDTO createUserDTO) {
    // Draft method to create a user
  }
}
