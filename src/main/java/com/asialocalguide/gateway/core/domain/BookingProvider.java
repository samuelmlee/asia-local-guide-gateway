package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BookingProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String name;
}
