package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.planning.Planning;
import com.asialocalguide.gateway.core.repository.custom.CustomPlanningRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long>, CustomPlanningRepository {}
