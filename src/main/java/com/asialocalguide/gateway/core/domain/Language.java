package com.asialocalguide.gateway.core.domain;

import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Language {

  @Id private Long id;

  @Column(nullable = false, unique = true, length = 2)
  @Enumerated(EnumType.STRING)
  private LanguageCode code;

  public Language(Long id, LanguageCode code) {
    if (id == null || code == null) {
      throw new IllegalArgumentException("Id and code cannot be null");
    }
    this.id = id;
    this.code = code;
  }
}
