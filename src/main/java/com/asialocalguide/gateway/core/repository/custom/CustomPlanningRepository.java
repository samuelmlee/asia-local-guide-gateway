package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.planning.Planning;
import com.asialocalguide.gateway.destination.domain.LanguageCode;

import java.util.List;
import java.util.UUID;

public interface CustomPlanningRepository {

	boolean existsByAppUserIdAndName(UUID appUserId, String name);

	List<Planning> getPlanningsByAppUserIdAndLanguageCode(UUID appUserId, LanguageCode languageCode);
}
