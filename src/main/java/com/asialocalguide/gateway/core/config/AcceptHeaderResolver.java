package com.asialocalguide.gateway.core.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Custom {@link AcceptHeaderLocaleResolver} that restricts locale resolution to the
 * application's {@link SupportedLocale} set.
 *
 * <p>Falls back to the JVM default locale when the {@code Accept-Language} header is absent,
 * blank, or does not match any supported locale.
 */
public class AcceptHeaderResolver extends AcceptHeaderLocaleResolver {

	List<Locale> locales = Arrays.stream(SupportedLocale.values()).map(l -> Locale.of(l.getCode())).toList();

	/**
	 * Resolves the locale from the {@code Accept-Language} request header.
	 *
	 * @param request the current HTTP request
	 * @return the best-matching supported locale, or the JVM default if none matches
	 */
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
