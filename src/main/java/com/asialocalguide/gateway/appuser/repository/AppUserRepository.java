package com.asialocalguide.gateway.appuser.repository;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.repository.custom.CustomAppUserRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link AppUser} entities.
 *
 * <p>Extends {@link JpaRepository} for standard CRUD operations and
 * {@link CustomAppUserRepository} for provider-based lookup queries.
 */
public interface AppUserRepository extends JpaRepository<AppUser, UUID>, CustomAppUserRepository {

	/**
	 * Returns the user with the given email address, if one exists.
	 *
	 * @param email the email address to search for
	 * @return an Optional containing the matching user, or empty if not found
	 */
	Optional<AppUser> findByEmail(String email);
}
