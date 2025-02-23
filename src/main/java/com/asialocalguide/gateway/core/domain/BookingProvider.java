package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BookingProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(unique = true, nullable = false)
  private BookingProviderName name;
}
