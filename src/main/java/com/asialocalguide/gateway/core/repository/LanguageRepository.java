package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Language} entities providing standard CRUD operations.
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
}
