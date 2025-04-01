package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.activitytag.ActivityTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityTagRepository extends JpaRepository<ActivityTag, Long>, CustomActivityTagRepository {


}
