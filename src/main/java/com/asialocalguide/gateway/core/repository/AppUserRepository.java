package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.user.AppUser;
import com.asialocalguide.gateway.core.repository.custom.CustomAppUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID>, CustomAppUserRepository {

  Optional<AppUser> findByEmail(String email);
}
