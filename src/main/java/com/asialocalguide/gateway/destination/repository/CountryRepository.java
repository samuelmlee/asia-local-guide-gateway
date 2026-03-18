package com.asialocalguide.gateway.destination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.destination.domain.Country;

import java.util.List;
import java.util.Set;

public interface CountryRepository extends JpaRepository<Country, Long> {

	List<Country> findByIso2CodeIn(Set<String> iso2Codes);

	@Transactional(readOnly = true)
	@Query("SELECT DISTINCT c.iso2Code FROM Country c")
	Set<String> findAllIso2Codes();
}
