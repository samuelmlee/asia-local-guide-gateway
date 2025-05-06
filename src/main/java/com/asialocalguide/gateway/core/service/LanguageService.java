package com.asialocalguide.gateway.core.service;

import com.asialocalguide.gateway.core.domain.Language;
import com.asialocalguide.gateway.core.repository.LanguageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LanguageService {

  private final LanguageRepository languageRepository;

  public LanguageService(LanguageRepository languageRepository) {
    this.languageRepository = languageRepository;
  }

  public List<Language> getAllLanguages() {
    return languageRepository.findAll();
  }
}
