package com.asialocalguide.gateway.core.config;

import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class LocaleConfig {

  @Bean
  public LocaleResolver sessionLocaleResolver() {
    AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderResolver();
    localeResolver.setDefaultLocale(Locale.ENGLISH);
    return new AcceptHeaderResolver();
  }
}
