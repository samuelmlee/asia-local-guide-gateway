package com.asialocalguide.gateway.core.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class UserAuth {

  @EmbeddedId private UserAuthId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @NotEmpty private String providerUserId;

  public UserAuth(User user, AuthProviderName authProviderName, String providerUserId) {
    if (user == null || authProviderName == null || providerUserId == null) {
      throw new IllegalArgumentException("User, AuthProviderName or providerUserId cannot be null");
    }

    this.id = new UserAuthId(user.getId(), authProviderName);
    this.user = user;
    this.providerUserId = providerUserId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserAuth userAuth = (UserAuth) o;
    return Objects.equals(getId(), userAuth.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
