package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

  @Query(
      "SELECT d FROM Destination d JOIN d.bookingProviderMappings bpm JOIN bookingProvider bp "
          + "WHERE bp.id = :bookingProviderId "
          + "AND bpm.providerDestinationId = :providerDestinationId")
  Destination findByProviderAndProviderDestinationId(
      @Param("bookingProviderId") Long bookingProviderId,
      @Param("providerDestinationId") String providerDestinationId);
}
