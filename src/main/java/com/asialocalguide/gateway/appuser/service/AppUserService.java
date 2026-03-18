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

	@Transactional(readOnly = true)
	public AppUser getAppUserById(UUID id) {

		return appUserRepository.findById(id)
				.orElseThrow(() -> new AppUserNotFoundException(String.format("User not found with id: %s", id)));
	}

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
