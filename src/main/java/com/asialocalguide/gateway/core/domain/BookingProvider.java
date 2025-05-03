package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class BookingProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(unique = true, nullable = false)
  private BookingProviderName name;

  public BookingProvider(BookingProviderName name) {
    this.name = name;
  }
}
