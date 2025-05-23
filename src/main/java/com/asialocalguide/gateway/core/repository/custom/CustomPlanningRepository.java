package com.asialocalguide.gateway.core.repository.custom;

import java.util.UUID;

public interface CustomPlanningRepository {

  boolean existsByAppUserIdAndName(UUID appUserId, String name);
}
