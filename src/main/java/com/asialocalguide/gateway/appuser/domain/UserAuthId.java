package com.asialocalguide.gateway.appuser.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/**
 * Composite primary key for {@link UserAuth}, combining the user's UUID and
 * the name of the authentication provider.
 */
@NoArgsConstructor
@Embeddable
@Getter
public class UserAuthId implements Serializable {
	@Column(name = "app_user_id")
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private AuthProviderName authProviderName;

	/**
	 * @param userId           the UUID of the application user
	 * @param authProviderName the authentication provider associated with the user
	 */
	public UserAuthId(UUID userId, AuthProviderName authProviderName) {
		this.userId = userId;
		this.authProviderName = authProviderName;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof UserAuthId))
			return false;
		UserAuthId that = (UserAuthId) o;
		return Objects.equals(getUserId(), that.getUserId()) && getAuthProviderName() == that.getAuthProviderName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUserId(), getAuthProviderName());
	}
}
