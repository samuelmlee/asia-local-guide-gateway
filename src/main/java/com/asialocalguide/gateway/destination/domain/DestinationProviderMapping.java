package com.asialocalguide.gateway.destination.domain;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.*;

/**
 * Maps a {@link Destination} to its provider-specific destination identifier for a given
 * {@link com.asialocalguide.gateway.core.domain.BookingProvider}.
 *
 * <p>Enables the gateway to translate internal destination IDs into the identifiers expected
 * by each external booking provider API.
 */
@Getter
@Entity
@NoArgsConstructor
public class DestinationProviderMapping {

	@EmbeddedId
	private DestinationProviderMappingId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destination_id", insertable = false, updatable = false)
	@NotNull
	private Destination destination;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_provider_id", insertable = false, updatable = false)
	@NotNull
	private BookingProvider provider;

	@NotEmpty
	private String providerDestinationId;

	/**
	 * @param destination           the destination this mapping belongs to; must not be {@code null}
	 * @param provider              the booking provider; must not be {@code null}
	 * @param providerDestinationId the provider's own destination identifier; must not be {@code null}
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public DestinationProviderMapping(Destination destination, BookingProvider provider, String providerDestinationId) {
		if (destination == null || provider == null || providerDestinationId == null) {
			throw new IllegalArgumentException(
					"Destination or BookingProvider or providerDestinationId cannot be null");
		}

		this.id = new DestinationProviderMappingId(destination.getId(), provider.getId());
		this.destination = destination;
		this.provider = provider;
		this.providerDestinationId = providerDestinationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DestinationProviderMapping that = (DestinationProviderMapping) o;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}
}
