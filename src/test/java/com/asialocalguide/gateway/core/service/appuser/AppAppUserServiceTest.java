package com.asialocalguide.gateway.core.service.appuser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.domain.user.UserAuth;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.exception.UserCreationException;
import com.asialocalguide.gateway.core.repository.AppAppUserRepository;
import com.asialocalguide.gateway.core.service.auth.AuthProviderService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class AppAppUserServiceTest {

  @Mock private AppAppUserRepository appUserRepository;

  @Mock private AuthProviderService authProviderService;

  @InjectMocks private AppUserService appUserService;

  @Test
  void createUserShouldSaveUserWhenAppUserDoesNotExistWithEmail() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "test@example.com", "Test User");

    when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    AppUser savedAppUser = new AppUser();
    savedAppUser.setEmail("test@example.com");

    when(appUserRepository.save(any(AppUser.class))).thenReturn(savedAppUser);

    // Act
    AppUser result = appUserService.createAppUser(createUserDTO);

    // Assert
    assertEquals("test@example.com", result.getEmail());

    ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
    verify(appUserRepository).save(userCaptor.capture());

    AppUser capturedAppUser = userCaptor.getValue();
    assertThat(capturedAppUser.getEmail()).isEqualTo("test@example.com");
    assertThat(capturedAppUser.getName()).isEqualTo("Test User");
  }

  @Test
  void createUserShouldThrowExceptionWhenAppUserAlreadyExists() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "existing@example.com", "Test User");

    AppUser existingAppUser = new AppUser();
    existingAppUser.setEmail("existing@example.com");

    when(appUserRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingAppUser));

    // Act & Assert
    UserCreationException exception =
        assertThrows(UserCreationException.class, () -> appUserService.createAppUser(createUserDTO));

    assertThat(exception.getMessage()).contains("User already exists with email");
    verify(appUserRepository, never()).save(any(AppUser.class));
  }

  @Test
  void createAppUserShouldThrowExceptionWhenDatabaseErrorOccurs() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "test@example.com", "Test User");

    when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(appUserRepository.save(any(AppUser.class))).thenThrow(new DataAccessException("Database error") {});

    // Act & Assert
    assertThatThrownBy(() -> appUserService.createAppUser(createUserDTO)).isInstanceOf(UserCreationException.class);
    verify(authProviderService, times(1)).deleteProviderUser(createUserDTO.providerUserId());
  }

  @Test
  void createUserShouldCreateAppUserWithCorrectAssociations() {
    // Arrange
    CreateUserDTO createUserDTO =
        new CreateUserDTO("firebase123", AuthProviderName.FIREBASE, "test@example.com", "Test User");

    when(appUserRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
    when(appUserRepository.save(userCaptor.capture())).thenAnswer(i -> i.getArgument(0));

    // Act
    appUserService.createAppUser(createUserDTO);

    // Assert
    AppUser capturedAppUser = userCaptor.getValue();
    assertThat(capturedAppUser.getEmail()).isEqualTo("test@example.com");
    assertThat(capturedAppUser.getName()).isEqualTo("Test User");

    assertThat(capturedAppUser.findUserAuth(AuthProviderName.FIREBASE)).isPresent();

    UserAuth userAuth =
        capturedAppUser
            .findUserAuth(AuthProviderName.FIREBASE)
            .orElseThrow(() -> new RuntimeException("UserAuth not found"));

    assertThat(userAuth.getProviderUserId()).isEqualTo("firebase123");
    assertThat(userAuth.getAppUser()).isEqualTo(capturedAppUser);
  }
}
