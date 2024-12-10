package com.asialocalguide.gateway.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Destination {

  @Id private Long destinationId;

  private String name;

  private DestinationType type;
}
