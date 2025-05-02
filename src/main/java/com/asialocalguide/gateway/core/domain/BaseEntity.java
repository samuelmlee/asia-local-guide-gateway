package com.asialocalguide.gateway.core.domain;

import com.asialocalguide.gateway.core.util.UuidUtils;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

  @Id private UUID id;

  protected BaseEntity() {
    this.id = UuidUtils.randomV7();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof BaseEntity baseEntity)) return false;
    return Objects.equals(getId(), baseEntity.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
