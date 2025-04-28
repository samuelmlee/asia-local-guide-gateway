package com.asialocalguide.gateway.core.domain.planning;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ActivityTranslation {

  @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
  private Activity activity;
}
