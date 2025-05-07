package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class BookingProvider {

  @Id private Long id;

  @Enumerated(EnumType.STRING)
  @Column(unique = true, nullable = false)
  private BookingProviderName name;

  public BookingProvider(Long id, BookingProviderName name) {
    if (id == null || name == null) {
      throw new IllegalArgumentException("ID or BookingProviderName cannot be null");
    }
    this.id = id;
    this.name = name;
  }
}
