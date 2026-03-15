package com.asialocalguide.gateway.core.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.repository.custom.CustomActivityRepository;

public interface ActivityRepository extends JpaRepository<Activity, UUID>, CustomActivityRepository {}
