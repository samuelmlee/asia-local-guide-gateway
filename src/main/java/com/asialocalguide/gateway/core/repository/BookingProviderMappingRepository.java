package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProviderMapping;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingProviderMappingRepository
    extends JpaRepository<BookingProviderMapping, Long> {

  @Query(
      "SELECT DISTINCT m.providerDestinationId FROM BookingProviderMapping m WHERE m.providerName = :providerName")
  Set<String> findProviderDestinationIdsByProviderName(
      @Param("providerName") BookingProviderName providerName);
}
