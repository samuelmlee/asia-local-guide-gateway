package com.asialocalguide.gateway.appuser.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Links an {@link AppUser} to an external authentication provider.
 *
 * <p>Stores the provider-assigned user ID (e.g. a Firebase UID) alongside the
 * composite key formed by the app user's UUID and the provider name.
 */
@Getter
@Entity
@NoArgsConstructor
public class UserAuth {

	@EmbeddedId
	private UserAuthId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "app_user_id", insertable = false, updatable = false)
	private AppUser appUser;

	@NotEmpty
	private String providerUserId;

	/**
	 * @param appUser          the application user this auth entry belongs to; must not be {@code null}
	 * @param authProviderName the authentication provider; must not be {@code null}
	 * @param providerUserId   the user identifier assigned by the provider; must not be {@code null}
	 * @throws IllegalArgumentException if any argument is {@code null}
	 */
	public UserAuth(AppUser appUser, AuthProviderName authProviderName, String providerUserId) {
		if (appUser == null || authProviderName == null || providerUserId == null) {
			throw new IllegalArgumentException("User, AuthProviderName or providerUserId cannot be null");
		}

		this.id = new UserAuthId(appUser.getId(), authProviderName);
		this.appUser = appUser;
		this.providerUserId = providerUserId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserAuth userAuth = (UserAuth) o;
		return Objects.equals(getId(), userAuth.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}
}
