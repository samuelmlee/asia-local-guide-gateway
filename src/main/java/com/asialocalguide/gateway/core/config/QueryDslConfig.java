package com.asialocalguide.gateway.core.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that exposes a {@link JPAQueryFactory} bean for QueryDSL-based repositories.
 */
@Configuration
public class QueryDslConfig {

	@PersistenceContext
	private EntityManager em;

	@Bean
	JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(em);
	}
}
