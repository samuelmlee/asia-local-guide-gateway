package com.asialocalguide.gateway.appuser.domain;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a registered application user with a unique email address.
 *
 * <p>A user may have one or more {@link UserAuth} entries that link the account
 * to external authentication providers (e.g. Firebase).
 */
@Entity
@NoArgsConstructor
public class AppUser extends BaseEntity {

	@NotNull
	@Getter
	@Setter
	@Email
	@Column(unique = true, nullable = false)
	private String email;

	@Getter
	@Setter
	private String name;

	@OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserAuth> userAuths = new HashSet<>();

	/**
	 * Associates the given {@link UserAuth} with this user.
	 *
	 * @param userAuth the authentication entry to add; ignored if {@code null}
	 */
	public void addUserAuth(UserAuth userAuth) {
		if (userAuth == null) {
			return;
		}
		userAuths.add(userAuth);
	}

	/**
	 * Returns the {@link UserAuth} entry for the given authentication provider, if present.
	 *
	 * @param authProviderName the provider to search for; returns empty if {@code null}
	 * @return an Optional containing the matching UserAuth, or empty if not found
	 */
	public Optional<UserAuth> findUserAuth(AuthProviderName authProviderName) {
		if (authProviderName == null || userAuths == null) {
			return Optional.empty();
		}

		return userAuths.stream()
				.filter(Objects::nonNull)
				.filter(ua -> ua.getId() != null && authProviderName.equals(ua.getId().getAuthProviderName()))
				.findFirst();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AppUser that = (AppUser) o;
		return Objects.equals(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}
}
