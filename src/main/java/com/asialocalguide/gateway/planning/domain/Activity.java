package com.asialocalguide.gateway.planning.domain;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * JPA entity representing a bookable activity from a specific provider.
 *
 * <p>Price and availability are fetched from the provider on demand and are not stored here.
 * Translations and cover images are managed as owned collections with cascade-all and orphan removal.
 */
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Activity extends BaseEntity {
	// Price and availability of an activity are fetched from the provider on demand

	@Column(name = "provider_activity_id", nullable = false)
	@Getter
	private String providerActivityId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_provider_id", nullable = false)
	@NotNull
	@Getter
	private BookingProvider provider;

	@OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
	@NotEmpty
	private Set<ActivityTranslation> activityTranslations = new HashSet<>();

	@OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ActivityImage> coverImages = new HashSet<>();

	@DecimalMin(value = "0.0")
	@DecimalMax(value = "5.0")
	private Float averageRating;

	@Min(value = 0)
	private Integer reviewCount;

	@Min(value = 1)
	private Integer durationMinutes;

	@NotBlank
	@URL
	private String bookingUrl;

	@LastModifiedDate
	private Instant lastUpdated;

	/**
	 * @param providerActivityId the provider's identifier for this activity; must not be {@code null}
	 * @param provider           the booking provider that owns this activity; must not be {@code null}
	 * @param averageRating      average rating in the range [0.0, 5.0], or {@code null} if unknown
	 * @param reviewCount        total number of reviews, or {@code null} if unknown
	 * @param durationMinutes    activity duration in minutes; must be at least 1
	 * @param bookingUrl         direct booking URL; must be a valid non-blank URL
	 */
	public Activity(String providerActivityId, BookingProvider provider, Float averageRating, Integer reviewCount,
			Integer durationMinutes, String bookingUrl) {

		this.providerActivityId = providerActivityId;
		this.provider = provider;
		this.averageRating = averageRating;
		this.reviewCount = reviewCount;
		this.durationMinutes = durationMinutes;
		this.bookingUrl = bookingUrl;
	}

	/**
	 * Returns an unmodifiable view of all translations for this activity.
	 *
	 * @return set of activity translations; never {@code null}
	 */
	public Set<ActivityTranslation> getActivityTranslations() {
		return Collections.unmodifiableSet(activityTranslations);
	}

	/**
	 * Returns an unmodifiable view of all cover images for this activity.
	 *
	 * @return set of activity images; never {@code null}
	 */
	public Set<ActivityImage> getCoverImages() {
		return Collections.unmodifiableSet(coverImages);
	}

	/**
	 * Adds a cover image to this activity, establishing the bidirectional association.
	 * Silently ignores {@code null} images.
	 *
	 * @param image the image to add
	 */
	public void addImage(ActivityImage image) {
		if (image == null) {
			return;
		}
		image.setActivity(this);
		coverImages.add(image);
	}

	/**
	 * Removes a cover image from this activity, clearing the bidirectional association.
	 * Silently ignores {@code null} images.
	 *
	 * @param image the image to remove
	 */
	public void removeImage(ActivityImage image) {
		if (image == null) {
			return;
		}
		image.setActivity(null);
		coverImages.remove(image);
	}

	/**
	 * Adds a translation to this activity.
	 * Silently ignores {@code null} translations.
	 *
	 * @param activityTranslation the translation to add
	 */
	public void addTranslation(ActivityTranslation activityTranslation) {
		if (activityTranslation == null) {
			return;
		}
		activityTranslations.add(activityTranslation);
	}
}
