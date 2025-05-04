package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.user.User;
import com.asialocalguide.gateway.core.repository.custom.CustomUserRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

  Optional<User> findByEmail(String email);
}
