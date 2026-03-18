package com.asialocalguide.gateway.core.service.bookingprovider;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.repository.BookingProviderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving {@link BookingProvider} entities.
 */
@Service
public class BookingProviderService {

	private final BookingProviderRepository bookingProviderRepository;

	/**
	 * @param bookingProviderRepository repository for booking provider lookups
	 */
	public BookingProviderService(BookingProviderRepository bookingProviderRepository) {
		this.bookingProviderRepository = bookingProviderRepository;
	}

	/**
	 * Returns the booking provider matching the given name, if one exists.
	 *
	 * @param providerName the provider name to look up
	 * @return an Optional containing the matching provider, or empty if not found
	 */
	public Optional<BookingProvider> getBookingProviderByName(BookingProviderName providerName) {
		return bookingProviderRepository.findByName(providerName);
	}

	/**
	 * Returns all booking providers persisted in the database.
	 *
	 * @return list of all {@link BookingProvider} entities; never {@code null}
	 */
	public List<BookingProvider> getAllBookingProviders() {
		return bookingProviderRepository.findAll();
	}
}
