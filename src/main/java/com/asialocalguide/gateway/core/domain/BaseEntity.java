package com.asialocalguide.gateway.core.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Base class for JPA entities that use a UUID version 7 primary key.
 *
 * <p>The ID is generated using {@code UUIDv7} (time-ordered epoch) from the
 * <a href="https://github.com/f4b6a3/uuid-creator">uuid-creator</a> library,
 * which produces monotonically increasing values suited for B-tree index performance.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	private UUID id;

	protected BaseEntity() {
		// UUIDv7 See uuid-creator https://github.com/f4b6a3/uuid-creator
		this.id = UuidCreator.getTimeOrderedEpoch();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof BaseEntity baseEntity))
			return false;
		return Objects.equals(getId(), baseEntity.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
