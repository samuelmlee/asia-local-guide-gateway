package com.asialocalguide.gateway.destination.repository;

import com.asialocalguide.gateway.destination.domain.Destination;
import com.asialocalguide.gateway.destination.repository.custom.CustomDestinationRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link Destination} entities.
 *
 * <p>Extends {@link JpaRepository} for standard CRUD operations and
 * {@link CustomDestinationRepository} for complex query-with-eager-loading lookups.
 */
@Repository
public interface DestinationRepository extends JpaRepository<Destination, UUID>, CustomDestinationRepository {

	/**
	 * Returns all destinations whose country's ISO 3166-1 alpha-2 code is in the given set.
	 *
	 * @param isoCodes the set of ISO codes to filter by
	 * @return list of matching destinations; never {@code null}
	 */
	@Transactional(readOnly = true)
	@Query("SELECT d FROM Destination d WHERE d.country.iso2Code IN :isoCodes")
	List<Destination> findByIsoCodes(@Param("isoCodes") Set<String> isoCodes);
}
