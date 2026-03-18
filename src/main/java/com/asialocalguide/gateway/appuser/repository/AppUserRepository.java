package com.asialocalguide.gateway.appuser.repository;

import com.asialocalguide.gateway.appuser.domain.AppUser;
import com.asialocalguide.gateway.appuser.repository.custom.CustomAppUserRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID>, CustomAppUserRepository {

	Optional<AppUser> findByEmail(String email);
}
