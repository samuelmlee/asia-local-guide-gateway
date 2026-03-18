package com.asialocalguide.gateway.activitytag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.activitytag.repository.custom.CustomActivityTagRepository;

/**
 * Repository for {@link ActivityTag} entities.
 *
 * <p>Extends {@link JpaRepository} for standard CRUD operations and
 * {@link CustomActivityTagRepository} for locale-aware translation queries.
 */
public interface ActivityTagRepository extends JpaRepository<ActivityTag, Long>, CustomActivityTagRepository {
}
