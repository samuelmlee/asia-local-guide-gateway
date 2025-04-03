package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Destination;
import com.asialocalguide.gateway.core.repository.custom.CustomDestinationRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long>, CustomDestinationRepository {

  @Transactional(readOnly = true)
  @Query("SELECT d FROM Destination d WHERE d.country.iso2Code IN :isoCodes")
  List<Destination> findByIsoCodes(@Param("isoCodes") Set<String> isoCodes);
}
