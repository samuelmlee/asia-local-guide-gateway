package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Destination;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

  @Query(
      "SELECT d FROM Destination d "
          + "JOIN d.destinationTranslations dt "
          + "WHERE dt.languageCode = :languageCode "
          + "AND LOWER(dt.name) LIKE LOWER(CONCAT('%', :name, '%')) "
          + "AND d.type IN ('CITY', 'REGION')")
  List<Destination> findCityOrRegionByTranslationsForLanguageCodeAndName(
      String languageCode, String name);
}
