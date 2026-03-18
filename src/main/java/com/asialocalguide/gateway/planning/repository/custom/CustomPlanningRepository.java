package com.asialocalguide.gateway.planning.repository.custom;

import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.planning.domain.Planning;

import java.util.List;
import java.util.UUID;

public interface CustomPlanningRepository {

	boolean existsByAppUserIdAndName(UUID appUserId, String name);

	List<Planning> getPlanningsByAppUserIdAndLanguageCode(UUID appUserId, LanguageCode languageCode);
}
