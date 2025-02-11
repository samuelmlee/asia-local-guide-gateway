package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Country;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
  List<Country> findByIso2CodeIn(Set<String> iso2Codes);
}
