package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.planning.Activity;
import com.asialocalguide.gateway.core.repository.custom.CustomActivityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long>, CustomActivityRepository {}
