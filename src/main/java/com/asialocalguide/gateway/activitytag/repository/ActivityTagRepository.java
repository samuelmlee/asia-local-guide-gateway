package com.asialocalguide.gateway.activitytag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asialocalguide.gateway.activitytag.domain.ActivityTag;
import com.asialocalguide.gateway.core.repository.custom.CustomActivityTagRepository;

public interface ActivityTagRepository extends JpaRepository<ActivityTag, Long>, CustomActivityTagRepository {
}
