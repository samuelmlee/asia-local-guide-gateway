package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.QDestinationProviderMapping;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CustomBookingProviderMappingRepositoryImpl implements CustomBookingProviderMappingRepository {

  private final JPAQueryFactory queryFactory;

  public CustomBookingProviderMappingRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName) {
    QDestinationProviderMapping mapping = QDestinationProviderMapping.destinationProviderMapping;

    List<String> results =
        queryFactory
            .select(mapping.providerDestinationId)
            .from(mapping)
            .where(mapping.provider.name.eq(providerName))
            .fetch();

    return new HashSet<>(results);
  }
}
