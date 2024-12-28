package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.Destination;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

  List<Destination>
      findByDestinationTranslations_LocaleAndDestinationTranslations_DestinationNameContainingIgnoreCase(
          String locale, String destinationName);
}
