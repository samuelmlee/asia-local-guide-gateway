package com.asialocalguide.gateway.destination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.destination.domain.Country;

import java.util.List;
import java.util.Set;

/**
 * Repository for {@link Country} entities.
 */
public interface CountryRepository extends JpaRepository<Country, Long> {

	/**
	 * Returns all countries whose ISO 3166-1 alpha-2 code is in the given set.
	 *
	 * @param iso2Codes the set of ISO codes to match
	 * @return list of matching countries; never {@code null}
	 */
	List<Country> findByIso2CodeIn(Set<String> iso2Codes);

	/**
	 * Returns the distinct set of ISO 3166-1 alpha-2 codes for all stored countries.
	 *
	 * @return set of ISO codes; never {@code null}
	 */
	@Transactional(readOnly = true)
	@Query("SELECT DISTINCT c.iso2Code FROM Country c")
	Set<String> findAllIso2Codes();
}
