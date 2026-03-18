package com.asialocalguide.gateway.planning.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asialocalguide.gateway.planning.domain.Planning;
import com.asialocalguide.gateway.planning.repository.custom.CustomPlanningRepository;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, UUID>, CustomPlanningRepository {
}
