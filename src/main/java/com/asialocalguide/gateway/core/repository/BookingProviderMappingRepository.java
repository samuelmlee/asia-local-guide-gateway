package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.DestinationProviderMapping;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingProviderMappingRepository extends JpaRepository<DestinationProviderMapping, Long> {

  @Query(
      """
      SELECT DISTINCT m.providerDestinationId
      FROM DestinationProviderMapping m
      WHERE m.provider.name = :providerName
      """)
  Set<String> findProviderDestinationIdsByProviderName(@Param("providerName") BookingProviderName providerName);
}
