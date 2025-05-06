package com.asialocalguide.gateway.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Language {

  @Id private Long id;

  @Column(nullable = false, unique = true, length = 2)
  @NotBlank
  @Size(max = 2)
  private String code;
}
