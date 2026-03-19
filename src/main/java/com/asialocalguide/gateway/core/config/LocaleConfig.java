package com.asialocalguide.gateway.core.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Spring configuration that registers the application's locale resolution strategy.
 *
 * <p>Installs {@link AcceptHeaderResolver} as the {@link org.springframework.web.servlet.LocaleResolver}
 * bean so all controllers resolve the request locale from the {@code Accept-Language} header.
 */
@Configuration
public class LocaleConfig {

	@Bean
	LocaleResolver sessionLocaleResolver() {
		AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderResolver();
		localeResolver.setDefaultLocale(Locale.ENGLISH);
		return new AcceptHeaderResolver();
	}
}
