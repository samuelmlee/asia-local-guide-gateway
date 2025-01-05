package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.ActivityTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityTagRepository extends JpaRepository<ActivityTag, Long> {}
