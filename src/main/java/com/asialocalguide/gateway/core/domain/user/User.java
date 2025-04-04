package com.asialocalguide.gateway.core.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

@Entity
public class User {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Getter @Email private String email;

  @Getter private String name;

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
