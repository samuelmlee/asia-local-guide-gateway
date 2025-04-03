package com.asialocalguide.gateway.core.repository.custom;

import com.asialocalguide.gateway.core.domain.destination.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CustomDestinationRepositoryImpl implements CustomDestinationRepository {

  private final JPAQueryFactory queryFactory;

  public CustomDestinationRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Destination> findCityOrRegionByNameWithEagerTranslations(LanguageCode languageCode, String name) {
    QDestination destination = QDestination.destination;
    QDestinationTranslation dt = QDestinationTranslation.destinationTranslation;
    QCountry country = QCountry.country;
    QCountryTranslation ct = QCountryTranslation.countryTranslation;

    return queryFactory
        .selectFrom(destination)
        .join(destination.destinationTranslations, dt)
        .fetchJoin()
        .join(destination.country, country)
        .fetchJoin()
        .join(country.countryTranslations, ct)
        .fetchJoin()
        .where(
            dt.id.languageCode.eq(languageCode),
            ct.id.languageCode.eq(languageCode),
            dt.name.lower().like("%" + name.toLowerCase() + "%"),
            destination.type.in(DestinationType.CITY, DestinationType.REGION))
        .fetch();
  }
}
