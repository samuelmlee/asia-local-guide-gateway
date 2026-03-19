package com.asialocalguide.gateway.appuser.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.domain.AuthProviderName;
import com.asialocalguide.gateway.appuser.domain.UserAuth;
import com.asialocalguide.gateway.appuser.dto.CreateAppUserDTO;
import com.asialocalguide.gateway.appuser.exception.AppUserCreationException;
import com.asialocalguide.gateway.appuser.exception.AppUserNotFoundException;
import com.asialocalguide.gateway.appuser.repository.AppUserRepository;
import com.asialocalguide.gateway.auth.service.AuthProviderService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for creating and retrieving application users.
 *
 * <p>On creation, the service verifies that no existing user shares the same email, persists the
 * new user with its {@link com.asialocalguide.gateway.appuser.domain.UserAuth} entry, and rolls
 * back the provider-side user registration if persistence fails.
 */
@Service
@Slf4j
public class AppUserService {

	private final AppUserRepository appUserRepository;

	private final AuthProviderService authProviderService;

	/**
	 * @param appUserRepository  repository for persisting and querying app users
	 * @param authProviderService service used to clean up provider-side users on failure
	 */
	public AppUserService(AppUserRepository appUserRepository, AuthProviderService authProviderService) {
		this.appUserRepository = appUserRepository;
		this.authProviderService = authProviderService;
	}

	/**
	 * Creates and persists a new application user from the given registration data.
	 *
	 * <p>If persistence fails after the provider user has already been created, the provider
	 * user is deleted as a best-effort compensating action before the exception is re-thrown.
	 *
	 * @param createAppUserDTO validated DTO containing the new user's details
	 * @return the persisted {@link AppUser}
	 * @throws AppUserCreationException if the email is already registered or persistence fails
	 */
	@Transactional
	public AppUser createAppUser(CreateAppUserDTO createAppUserDTO) {
		checkAppUserExistence(createAppUserDTO);

		try {
			return persistNewAppUser(createAppUserDTO);

		} catch (Exception e) {

			log.warn("Failed to create app user for email: {}. Deleting provider user for provider: {}",
					createAppUserDTO.email(),
					createAppUserDTO.providerName());
			
			deleteProviderUser(createAppUserDTO);

			throw new AppUserCreationException(String.format("Failed to create user."), e);
		}
	}

	private void checkAppUserExistence(CreateAppUserDTO createUserDTO) {
		try {
			Optional<AppUser> userOpt = appUserRepository.findByEmail(createUserDTO.email());

			if (userOpt.isPresent()) {
				throw new AppUserCreationException(
						String.format("User already exists with email: %s", createUserDTO.email()));
			}

		} catch (AppUserCreationException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new AppUserCreationException(String.format("Failed to check user existence: %s", createUserDTO), ex);
		}
	}

	/**
	 * Returns the application user with the given ID.
	 *
	 * @param id the UUID of the user to retrieve
	 * @return the matching {@link AppUser}
	 * @throws AppUserNotFoundException if no user exists with the given ID
	 */
	@Transactional(readOnly = true)
	public AppUser getAppUserById(UUID id) {

		return appUserRepository.findById(id)
				.orElseThrow(() -> new AppUserNotFoundException(String.format("User not found with id: %s", id)));
	}

	/**
	 * Returns the application user linked to the given provider and provider-assigned user ID.
	 *
	 * @param providerName   the authentication provider; returns empty if {@code null}
	 * @param providerUserId the provider-assigned user identifier; returns empty if {@code null}
	 * @return an Optional containing the matching user, or empty if not found or inputs are null
	 */
	@Transactional(readOnly = true)
	public Optional<AppUser> getUserByProviderNameAndProviderUserId(AuthProviderName providerName,
			String providerUserId) {
		if (providerName == null || providerUserId == null) {
			return Optional.empty();
		}

		return appUserRepository.findUserByProviderNameAndProviderUserId(providerName, providerUserId);
	}

	private AppUser persistNewAppUser(CreateAppUserDTO createUserDTO) {
		AppUser appUser = new AppUser();
		appUser.setEmail(createUserDTO.email());
		appUser.setName(createUserDTO.name());

		UserAuth userAuth = new UserAuth(appUser, createUserDTO.providerName(), createUserDTO.providerUserId());
		appUser.addUserAuth(userAuth);

		return appUserRepository.save(appUser);
	}

	private void deleteProviderUser(CreateAppUserDTO createUserDTO) {
		try {

			authProviderService.deleteProviderUser(createUserDTO.providerUserId());
		} catch (Exception ex) {
			
			log.error("Failed to delete provider user after app user creation failure for provider: {}",
					createUserDTO.providerName(),
					ex);
		}
	}
}
