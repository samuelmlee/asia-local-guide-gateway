package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.BookingProvider;
import com.asialocalguide.gateway.core.domain.BookingProviderName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface BookingProviderRepository extends JpaRepository<BookingProvider, Long> {

    @Transactional(readOnly = true)
    Optional<BookingProvider> findByName(BookingProviderName name);
}
