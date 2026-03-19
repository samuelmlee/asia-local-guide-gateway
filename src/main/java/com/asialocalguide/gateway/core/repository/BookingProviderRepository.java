package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository for {@link BookingProvider} entities.
 */
@Repository
public interface BookingProviderRepository extends JpaRepository<BookingProvider, Long> {

	/**
	 * Returns the booking provider with the given name, if one exists.
	 *
	 * @param name the provider name to search for
	 * @return an Optional containing the matching provider, or empty if not found
	 */
	@Transactional(readOnly = true)
	Optional<BookingProvider> findByName(BookingProviderName name);
}
