package com.asialocalguide.gateway.core.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

public class AcceptHeaderResolver extends AcceptHeaderLocaleResolver {

	List<Locale> locales = Arrays.stream(SupportedLocale.values()).map(l -> Locale.of(l.getCode())).toList();

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		String headerLang = request.getHeader("Accept-Language");
		if (headerLang == null || headerLang.isEmpty()) {
			return Locale.getDefault();
		}

		try {
			Locale resolvedLocale = Locale.lookup(Locale.LanguageRange.parse(headerLang), locales);

			if (resolvedLocale == null) {
				return Locale.getDefault();
			}

			return resolvedLocale;

		} catch (IllegalArgumentException e) {
			return Locale.getDefault();
		}
	}
}
