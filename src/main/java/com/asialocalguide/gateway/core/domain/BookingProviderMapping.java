package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BookingProviderMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private BookingProvider bookingProvider;

  private String providerDestinationId;
}
