package com.asialocalguide.gateway.core.service.appuser;

import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.domain.user.AuthProviderName;
import com.asialocalguide.gateway.core.domain.user.UserAuth;
import com.asialocalguide.gateway.core.dto.user.CreateUserDTO;
import com.asialocalguide.gateway.core.exception.UserCreationException;
import com.asialocalguide.gateway.core.exception.UserNotFoundException;
import com.asialocalguide.gateway.core.repository.AppUserRepository;
import com.asialocalguide.gateway.core.service.auth.AuthProviderService;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AppUserService {

  private final AppUserRepository appUserRepository;

  private final AuthProviderService authProviderService;

  public AppUserService(AppUserRepository appUserRepository, AuthProviderService authProviderService) {
    this.appUserRepository = appUserRepository;
    this.authProviderService = authProviderService;
  }

  @Transactional
  public AppUser createAppUser(CreateUserDTO createUserDTO) {
    checkAppUserExistence(createUserDTO);

    try {
      return persistNewAppUser(createUserDTO);

    } catch (Exception e) {

      log.warn(
          "Failed to create app userfor email: {}. Deleting provider user: {}",
          createUserDTO.email(),
          createUserDTO.providerUserId());
      deleteProviderUser(createUserDTO);

      throw new UserCreationException(String.format("Failed to create user : %s", createUserDTO), e);
    }
  }

  private void checkAppUserExistence(CreateUserDTO createUserDTO) {
    try {
      Optional<AppUser> userOpt = appUserRepository.findByEmail(createUserDTO.email());

      if (userOpt.isPresent()) {
        throw new UserCreationException(String.format("User already exists with email: %s", createUserDTO.email()));
      }

    } catch (UserCreationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new UserCreationException(String.format("Failed to check user existence: %s", createUserDTO), ex);
    }
  }

  @Transactional(readOnly = true)
  public AppUser getAppUserById(UUID id) {

    return appUserRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException(String.format("User not found with id: %s", id)));
  }

  @Transactional(readOnly = true)
  public Optional<AppUser> getUserByProviderNameAndProviderUserId(
      AuthProviderName providerName, String providerUserId) {
    if (providerName == null || providerUserId == null) {
      return Optional.empty();
    }

    return appUserRepository.findUserByProviderNameAndProviderUserId(providerName, providerUserId);
  }

  private AppUser persistNewAppUser(CreateUserDTO createUserDTO) {
    AppUser appUser = new AppUser();
    appUser.setEmail(createUserDTO.email());
    appUser.setName(createUserDTO.name());

    UserAuth userAuth = new UserAuth(appUser, createUserDTO.providerName(), createUserDTO.providerUserId());
    appUser.addUserAuth(userAuth);

    return appUserRepository.save(appUser);
  }

  private void deleteProviderUser(CreateUserDTO createUserDTO) {
    try {

      authProviderService.deleteProviderUser(createUserDTO.providerUserId());
    } catch (Exception ex) {
      log.error(
          "Failed to delete provider user with email: {} for provider: {} after failing to create app user",
          createUserDTO.email(),
          createUserDTO.providerName(),
          ex);
    }
  }
}
