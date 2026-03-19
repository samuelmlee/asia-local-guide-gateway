package com.asialocalguide.gateway.planning.domain;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.validator.constraints.URL;

/**
 * JPA entity representing a cover image for an {@link Activity}.
 *
 * <p>Images are owned by {@code Activity} and removed via orphan removal when detached.
 */
@Entity
@NoArgsConstructor
@Getter
public class ActivityImage extends BaseEntity {

	@ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
	@JoinColumn(name = "activity_id", nullable = false)
	private Activity activity;

	@NotNull
	@Positive
	private Integer height;

	@NotNull
	@Positive
	private Integer width;

	@NotBlank
	@URL
	private String url;

	@NotNull
	@Enumerated(EnumType.STRING)
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private ImageType type;

	/**
	 * @param height pixel height of the image; must be positive
	 * @param width  pixel width of the image; must be positive
	 * @param url    publicly accessible image URL; must be a valid non-blank URL
	 * @param type   the intended display context (desktop or mobile); must not be {@code null}
	 */
	public ActivityImage(Integer height, Integer width, String url, ImageType type) {
		this.height = height;
		this.width = width;
		this.url = url;
		this.type = type;
	}

	void setActivity(Activity activity) {
		this.activity = activity;
	}
}
