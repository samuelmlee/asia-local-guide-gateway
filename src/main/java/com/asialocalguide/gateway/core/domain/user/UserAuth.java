package com.asialocalguide.gateway.core.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class UserAuth {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @NotNull
  private AuthProviderName authProviderName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @NotNull
  private User user;

  @NotNull private String providerUserId;

  public UserAuth(User user, AuthProviderName authProviderName, String providerUserId) {
    this.user = user;
    this.authProviderName = authProviderName;
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
