package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

  @Query(
      """
      SELECT d FROM Destination d
      JOIN d.destinationTranslations dt
      WHERE dt.languageCode = :languageCode
      AND LOWER(dt.name) LIKE LOWER(CONCAT('%', :name, '%'))
      AND d.type IN ('CITY', 'REGION')
      """)
  List<Destination> findCityOrRegionByTranslationsForLanguageCodeAndName(LanguageCode languageCode, String name);

  @Query("SELECT d FROM Destination d WHERE d.country.iso2Code IN :isoCodes")
  List<Destination> findByCountryIsoCodes(@Param("isoCodes") Set<String> isoCodes);
}
