package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingProviderRepository extends JpaRepository<BookingProvider, Long> {
  BookingProvider findByName(BookingProviderType type);
}
