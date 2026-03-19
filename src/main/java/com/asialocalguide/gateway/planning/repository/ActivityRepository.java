package com.asialocalguide.gateway.planning.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.planning.domain.Activity;
import com.asialocalguide.gateway.planning.repository.custom.CustomActivityRepository;

/**
 * Spring Data JPA repository for {@link Activity} entities.
 *
 * <p>Extends {@link CustomActivityRepository} for QueryDSL-based lookups by provider name and ID.
 */
public interface ActivityRepository extends JpaRepository<Activity, UUID>, CustomActivityRepository {
}
