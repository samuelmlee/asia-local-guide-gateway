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
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private User user;

  @NotEmpty private String providerUserId;

  public UserAuth(User user, AuthProviderName authProviderName, String providerUserId) {

    this.id = new UserAuthId(authProviderName);
    this.user = user;
    this.providerUserId = providerUserId;
  }

  void setUser(User user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserAuth userAuth = (UserAuth) o;
    return Objects.equals(id, userAuth.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
