package com.asialocalguide.gateway.core.repository;

import com.asialocalguide.gateway.core.domain.destination.Country;
import com.asialocalguide.gateway.core.domain.destination.LanguageCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @Transactional(readOnly = true)
    @Query(
            """
                    SELECT c FROM Country c
                    JOIN FETCH c.countryTranslations ct
                    WHERE ct.id.languageCode = :languageCode
                    """)
    List<Country> findAllWithEagerTranslations(LanguageCode languageCode);

    List<Country> findByIso2CodeIn(Set<String> iso2Codes);

    @Transactional(readOnly = true)
    @Query("SELECT DISTINCT c.iso2Code FROM Country c")
    Set<String> findAllIso2Codes();
}
