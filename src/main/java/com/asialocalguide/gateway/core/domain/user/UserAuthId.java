package com.asialocalguide.gateway.core.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Embeddable
@Getter
public class UserAuthId implements Serializable {
  @Column(name = "user_id")
  private UUID userId;

  @Enumerated(EnumType.STRING)
  private AuthProviderName authProviderName;

  public UserAuthId(UUID userId, AuthProviderName authProviderName) {
    this.userId = userId;
    this.authProviderName = authProviderName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    UserAuthId that = (UserAuthId) o;
    return Objects.equals(getUserId(), that.getUserId()) && getAuthProviderName() == that.getAuthProviderName();
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(getUserId());
    result = 31 * result + Objects.hashCode(getAuthProviderName());
    return result;
  }
}
