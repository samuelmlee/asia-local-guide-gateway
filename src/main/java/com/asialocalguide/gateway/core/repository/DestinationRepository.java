package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.Destination;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

  @Query(
      "SELECT d FROM Destination d "
          + "JOIN d.destinationTranslations dt "
          + "WHERE dt.locale = :locale "
          + "AND LOWER(dt.destinationName) LIKE LOWER(CONCAT('%', :destinationName, '%')) "
          + "AND d.type IN ('CITY', 'REGION')")
  List<Destination> findCityAndRegionByTranslationsForLocaleAndDestinationName(
      String locale, String destinationName);
}
