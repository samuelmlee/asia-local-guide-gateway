package com.asialocalguide.gateway.core.domain.destination;

/*
 * This enum is mapped to the Language entity's code attribute in the database
 * To add new languages:
 *
 * 1. Add the two-letter ISO code in the enum
 * 2. Make sure the Language table has corresponding entries
 */
public enum LanguageCode {
  EN,
  FR;

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
