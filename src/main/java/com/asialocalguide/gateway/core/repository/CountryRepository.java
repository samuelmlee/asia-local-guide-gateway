package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Country;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CountryRepository extends JpaRepository<Country, Long> {
  List<Country> findByIso2CodeIn(Set<String> iso2Codes);

  @Query("SELECT DISTINCT c.iso2Code FROM Country c")
  Set<String> findAllIso2Codes();
}
