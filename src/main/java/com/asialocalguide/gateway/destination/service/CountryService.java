package com.asialocalguide.gateway.destination.service;

import com.asialocalguide.gateway.destination.domain.Country;
import com.asialocalguide.gateway.destination.repository.CountryRepository;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Service for querying {@link Country} data.
 */
@Service
public class CountryService {

	private final CountryRepository countryRepository;

	/**
	 * @param countryRepository repository for country lookups
	 */
	public CountryService(CountryRepository countryRepository) {
		this.countryRepository = countryRepository;
	}

	/**
	 * Returns countries whose ISO 3166-1 alpha-2 code is in the given set.
	 *
	 * @param iso2Codes the codes to filter by
	 * @return list of matching countries; never {@code null}
	 */
	public List<Country> findByIso2CodeIn(Set<String> iso2Codes) {
		return countryRepository.findByIso2CodeIn(iso2Codes);
	}

	/**
	 * Returns the set of ISO 3166-1 alpha-2 codes for all countries in the database.
	 *
	 * @return set of ISO codes; never {@code null}
	 */
	public Set<String> findAllIso2Codes() {
		return countryRepository.findAllIso2Codes();
	}
}
