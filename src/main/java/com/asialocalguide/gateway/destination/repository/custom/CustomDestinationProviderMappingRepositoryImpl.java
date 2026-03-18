package com.asialocalguide.gateway.destination.repository.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.QBookingProvider;
import com.asialocalguide.gateway.destination.domain.QDestinationProviderMapping;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * QueryDSL-based implementation of {@link CustomDestinationProviderMappingRepository}.
 */
@Repository
public class CustomDestinationProviderMappingRepositoryImpl implements CustomDestinationProviderMappingRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * @param queryFactory the QueryDSL factory used to build and execute JPQL queries
	 */
	public CustomDestinationProviderMappingRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	/** {@inheritDoc} */
	@Override
	@Transactional(readOnly = true)
	public Set<String> findProviderDestinationIdsByProviderName(BookingProviderName providerName) {
		QDestinationProviderMapping mapping = QDestinationProviderMapping.destinationProviderMapping;
		QBookingProvider provider = QBookingProvider.bookingProvider;

		List<String> results = queryFactory.select(mapping.providerDestinationId)
				.from(mapping)
				.innerJoin(mapping.provider, provider)
				.where(provider.name.eq(providerName))
				.fetch();

		return new HashSet<>(results);
	}
}
