package com.asialocalguide.gateway.core.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

public class AcceptHeaderResolver extends AcceptHeaderLocaleResolver {

  List<Locale> LOCALES =
      Arrays.stream(SupportedLocale.values()).map(l -> Locale.of(l.getCode())).toList();

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    String headerLang = request.getHeader("Accept-Language");
    return headerLang == null || headerLang.isEmpty()
        ? Locale.getDefault()
        : Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES);
  }
}
