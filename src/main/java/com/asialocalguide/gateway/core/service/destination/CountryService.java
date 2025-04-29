package com.asialocalguide.gateway.core.service.destination;

import com.asialocalguide.gateway.core.domain.destination.Country;
import com.asialocalguide.gateway.core.repository.CountryRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

  private final CountryRepository countryRepository;

  public CountryService(CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  public List<Country> findByIso2CodeIn(Set<String> iso2Codes) {
    return countryRepository.findByIso2CodeIn(iso2Codes);
  }

  public Set<String> findAllIso2Codes() {
    return countryRepository.findAllIso2Codes();
  }
}
