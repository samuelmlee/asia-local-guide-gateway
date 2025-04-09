package com.asialocalguide.gateway.core.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
public class User {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Getter
  @Setter
  @Email
  @Column(unique = true, nullable = false)
  private String email;

  @Getter @Setter private String name;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserAuth> userAuths = new HashSet<>();

  public void addUserAuth(UserAuth userAuth) {
    if (userAuth == null) {
      return;
    }
    userAuth.setUser(this);
    userAuths.add(userAuth);
  }

  public void removeUserAuth(UserAuth userAuth) {
    if (userAuth == null) {
      return;
    }
    userAuth.setUser(null);
    userAuths.remove(userAuth);
  }

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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User that = (User) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
