package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class BookingProvider {

  @Id private Long id;

  @Enumerated(EnumType.STRING)
  private BookingProviderType name;
}
