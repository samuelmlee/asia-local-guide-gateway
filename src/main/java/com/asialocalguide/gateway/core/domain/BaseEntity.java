package com.asialocalguide.gateway.core.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

  @Id private UUID id;

  protected BaseEntity() {
    // UUIDv7 See https://github.com/f4b6a3/uuid-creator
    this.id = UuidCreator.getTimeOrderedEpoch();
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
