package com.asialocalguide.gateway.core.domain.activitytag;


import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
public class ActivityTagProviderMapping {

    /* No bidirectional relationship for ActivityTag,
    it will need to be set manually in ActivityTagProviderMappingId */
    @EmbeddedId
    @Setter
    private ActivityTagProviderMappingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingProviderId")
    @JoinColumn(name = "booking_provider_id")
    @NotNull
    private BookingProvider provider;

    @Setter
    @NotNull
    @NotEmpty
    private String providerActivityTagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityTagProviderMapping that = (ActivityTagProviderMapping) o;

        if (id != null && that.id != null) {
            return id.equals(that.id);
        }

        return Objects.equals(provider, that.provider) &&
                Objects.equals(providerActivityTagId, that.providerActivityTagId);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }

        return Objects.hash(provider, providerActivityTagId);
    }
}
