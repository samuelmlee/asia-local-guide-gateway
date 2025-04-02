package com.asialocalguide.gateway.core.domain.destination;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@Embeddable
@Getter
@Setter
public class CountryTranslationId {
    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "language_code")
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode languageCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryTranslationId that = (CountryTranslationId) o;
        return Objects.equals(countryId, that.countryId) &&
                Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryId, languageCode);
    }
}
