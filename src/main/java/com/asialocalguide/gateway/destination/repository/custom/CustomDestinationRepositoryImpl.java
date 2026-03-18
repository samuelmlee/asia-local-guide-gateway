package com.asialocalguide.gateway.destination.repository.custom;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.asialocalguide.gateway.core.domain.QLanguage;
import com.asialocalguide.gateway.destination.domain.Destination;
import com.asialocalguide.gateway.destination.domain.DestinationType;
import com.asialocalguide.gateway.destination.domain.LanguageCode;
import com.asialocalguide.gateway.destination.domain.QCountry;
import com.asialocalguide.gateway.destination.domain.QCountryTranslation;
import com.asialocalguide.gateway.destination.domain.QDestination;
import com.asialocalguide.gateway.destination.domain.QDestinationTranslation;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
		QLanguage languageDt = new QLanguage("languageDt");
		QLanguage languageCt = new QLanguage("languageCt");

		return queryFactory.selectDistinct(destination)
				.from(destination)
				.join(destination.destinationTranslations, dt)
				.fetchJoin()
				.join(dt.language, languageDt)
				.fetchJoin()
				.join(destination.country, country)
				.fetchJoin()
				.join(country.countryTranslations, ct)
				.fetchJoin()
				.join(ct.language, languageCt)
				.fetchJoin()
				.where(dt.language.code.eq(languageCode),
						ct.language.code.eq(languageCode),
						dt.name.lower().like("%" + name.toLowerCase() + "%"),
						destination.type.in(DestinationType.CITY, DestinationType.REGION))
				.fetch();
	}
}
