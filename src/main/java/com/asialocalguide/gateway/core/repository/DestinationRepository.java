package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    @Transactional(readOnly = true)
    @Query(
            """
                    SELECT d FROM Destination d
                    JOIN d.destinationTranslations dt
                    WHERE dt.id.languageCode = :languageCode
                    AND LOWER(dt.name) LIKE LOWER(CONCAT('%', :name, '%'))
                    AND d.type IN ('CITY', 'REGION')
                    """)
    List<Destination> findCityOrRegionByTranslationsForLanguageCodeAndName(LanguageCode languageCode, String name);

    @Transactional(readOnly = true)
    @Query("SELECT d FROM Destination d WHERE d.country.iso2Code IN :isoCodes")
    List<Destination> findByIsoCodes(@Param("isoCodes") Set<String> isoCodes);
}
