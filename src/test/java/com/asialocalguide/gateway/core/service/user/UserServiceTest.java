package com.asialocalguide.gateway.core.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.domain.user.User;
import com.asialocalguide.gateway.core.domain.user.UserAuth;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.exception.UserCreationException;
import com.asialocalguide.gateway.core.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  void createUserShouldSaveUserWhenUserDoesNotExistWithEmail() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "test@example.com", "Test User");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    User savedUser = new User();
    savedUser.setEmail("test@example.com");

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    // Act
    User result = userService.createUser(createUserDTO);

    // Assert
    assertEquals("test@example.com", result.getEmail());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User capturedUser = userCaptor.getValue();
    assertEquals("test@example.com", capturedUser.getEmail());
    assertEquals("Test User", capturedUser.getName());
  }

  @Test
  void createUserShouldThrowExceptionWhenUserAlreadyExists() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "existing@example.com", "Test User");

    User existingUser = new User();
    existingUser.setEmail("existing@example.com");

    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

    // Act & Assert
    UserCreationException exception =
        assertThrows(UserCreationException.class, () -> userService.createUser(createUserDTO));

    assertTrue(exception.getMessage().contains("User already exists with email"));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void createUserShouldThrowExceptionWhenDatabaseErrorOccurs() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "test@example.com", "Test User");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("Database error") {});

    // Act & Assert
    UserCreationException exception =
        assertThrows(UserCreationException.class, () -> userService.createUser(createUserDTO));

    assertTrue(exception.getMessage().contains("Failed to create user"));
    assertInstanceOf(DataAccessException.class, exception.getCause());
  }

  @Test
  void createUserShouldCreateUserWithCorrectAssociations() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "test@example.com", "Test User");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(userRepository.save(userCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    // Act
    userService.createUser(createUserDTO);

    // Assert
    User capturedUser = userCaptor.getValue();
    assertEquals("test@example.com", capturedUser.getEmail());
    assertEquals("Test User", capturedUser.getName());

    assertTrue(capturedUser.findUserAuth(AuthProviderName.FIREBASE).isPresent());

    UserAuth userAuth =
        capturedUser
            .findUserAuth(AuthProviderName.FIREBASE)
            .orElseThrow(() -> new RuntimeException("UserAuth not found"));

    assertEquals("firebase123", userAuth.getProviderUserId());
    assertEquals(capturedUser, userAuth.getUser());
  }
}
