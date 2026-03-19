package com.asialocalguide.gateway.activitytag.domain;

import java.util.Objects;

import com.asialocalguide.gateway.core.domain.BookingProvider;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps an {@link ActivityTag} to its provider-specific tag identifier for a given
 * {@link com.asialocalguide.gateway.core.domain.BookingProvider}.
 *
 * <p>Allows the gateway to translate internal activity tags to the tag IDs expected
 * by each external booking provider.
 */
@Entity
@Getter
@NoArgsConstructor
public class ActivityTagProviderMapping {

	@EmbeddedId
	@Setter
	private ActivityTagProviderMappingId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "activity_tag_id", insertable = false, updatable = false)
	@NotNull
	private ActivityTag activityTag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_provider_id", insertable = false, updatable = false)
	@NotNull
	private BookingProvider provider;

	@Setter
	@NotNull
	@NotEmpty
	private String providerActivityTagId;

	/**
	 * @param activityTag           the internal activity tag; must not be {@code null}
	 * @param provider              the booking provider this mapping belongs to; must not be {@code null}
	 * @param providerActivityTagId the provider's own tag identifier; must not be {@code null}
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public ActivityTagProviderMapping(ActivityTag activityTag, BookingProvider provider, String providerActivityTagId) {
		if (activityTag == null || provider == null || providerActivityTagId == null) {
			throw new IllegalArgumentException(
					String.format("ActivityTag: %s, BookingProvider: %s or providerActivityTagId: %s cannot be null",
							activityTag,
							provider,
							providerActivityTagId));
		}

		this.id = new ActivityTagProviderMappingId(activityTag.getId(), provider.getId());
		this.activityTag = activityTag;
		this.provider = provider;
		this.providerActivityTagId = providerActivityTagId;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ActivityTagProviderMapping that))
			return false;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}
}
