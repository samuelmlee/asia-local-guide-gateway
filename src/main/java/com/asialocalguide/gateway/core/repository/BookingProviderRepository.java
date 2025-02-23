package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingProviderRepository extends JpaRepository<BookingProvider, Long> {

  Optional<BookingProvider> findByName(BookingProviderName name);
}
