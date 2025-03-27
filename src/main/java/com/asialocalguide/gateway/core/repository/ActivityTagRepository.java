package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.activitytag.ActivityTag;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityTagRepository extends JpaRepository<ActivityTag, Long> {

    @Query(""" 
            SELECT DISTINCT at FROM ActivityTag at
            LEFT JOIN FETCH at.activityTagTranslations tr
            WHERE tr.id.languageCode = :languageCode
            """)
    List<ActivityTag> findAllWithTranslations(@Param("languageCode") LanguageCode languageCode);
}
