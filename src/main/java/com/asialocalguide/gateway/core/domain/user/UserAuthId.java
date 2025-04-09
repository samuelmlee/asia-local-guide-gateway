package com.asialocalguide.gateway.core.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class UserAuthId implements Serializable {
  @Column(name = "user_id")
  private Long userId;

  @Enumerated(EnumType.STRING)
  private AuthProviderName authProviderName;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    UserAuthId that = (UserAuthId) o;
    return Objects.equals(userId, that.userId) && authProviderName == that.authProviderName;
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(userId);
    result = 31 * result + Objects.hashCode(authProviderName);
    return result;
  }
}
