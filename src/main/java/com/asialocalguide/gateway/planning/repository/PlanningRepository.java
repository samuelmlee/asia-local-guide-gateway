package com.asialocalguide.gateway.planning.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asialocalguide.gateway.planning.domain.Planning;
import com.asialocalguide.gateway.planning.repository.custom.CustomPlanningRepository;

/**
 * Spring Data JPA repository for {@link Planning} entities.
 *
 * <p>Extends {@link CustomPlanningRepository} for QueryDSL-based lookups by user and language.
 */
@Repository
public interface PlanningRepository extends JpaRepository<Planning, UUID>, CustomPlanningRepository {
}
