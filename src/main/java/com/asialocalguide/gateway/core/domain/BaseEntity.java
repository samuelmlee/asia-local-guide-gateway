package com.asialocalguide.gateway.core.domain;

import com.asialocalguide.gateway.core.util.UuidUtils;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

  @Id @Getter private UUID id;

  protected BaseEntity() {
    this.id = UuidUtils.randomV7();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseEntity that = (BaseEntity) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
