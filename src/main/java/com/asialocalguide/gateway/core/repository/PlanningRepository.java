package com.asialocalguide.gateway.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asialocalguide.gateway.core.domain.planning.Planning;
import com.asialocalguide.gateway.core.repository.custom.CustomPlanningRepository;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, UUID>, CustomPlanningRepository {
}
