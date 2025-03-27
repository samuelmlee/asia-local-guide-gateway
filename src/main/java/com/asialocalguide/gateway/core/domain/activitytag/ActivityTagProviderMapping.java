package com.asialocalguide.gateway.core.domain.activitytag;


import com.asialocalguide.gateway.core.domain.BookingProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class ActivityTagProviderMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_tag_id", nullable = false)
    @Setter
    @NotNull
    private ActivityTag activityTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @NotNull
    @Setter
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
