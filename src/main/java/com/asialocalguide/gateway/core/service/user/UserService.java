package com.asialocalguide.gateway.core.service.user;

import com.asialocalguide.gateway.core.domain.user.User;
import com.asialocalguide.gateway.core.domain.user.UserAuth;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.exception.UserCreationException;
import com.asialocalguide.gateway.core.exception.UserNotFoundException;
import com.asialocalguide.gateway.core.repository.UserRepository;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User createUser(CreateUserDTO createUserDTO) {
    try {
      Optional<User> userOptional = userRepository.findByEmail(createUserDTO.email());

      if (userOptional.isPresent()) {
        throw new UserCreationException(String.format("User already exists with email: %s", createUserDTO.email()));
      }

      return persistNewUser(createUserDTO);

    } catch (DataAccessException e) {
      throw new UserCreationException(
          String.format("Failed to create user : %s, with database exception  ", createUserDTO), e);
    }
  }

  public User getUserById(Long id) {

    return userRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException(String.format("User not found with id: %d", id)));
  }

  /**
   * This method is used to get a reference to the user entity to efficiently set the user in a Many To One
   * relationship. Using getReferenceById fetches the User lazily and requires a transaction to be open
   *
   * @param id The ID of the user.
   * @return An Optional containing the user reference if it exists, or an empty Optional if it doesn't.
   */
  public Optional<User> getUserReferenceById(Long id) {
    return Optional.of(userRepository.getReferenceById(id));
  }

  private User persistNewUser(CreateUserDTO createUserDTO) {
    User user = new User();
    user.setEmail(createUserDTO.email());
    user.setName(createUserDTO.name());

    UserAuth userAuth = new UserAuth(user, createUserDTO.providerName(), createUserDTO.providerUserId());
    user.addUserAuth(userAuth);

    return userRepository.save(user);
  }
}
