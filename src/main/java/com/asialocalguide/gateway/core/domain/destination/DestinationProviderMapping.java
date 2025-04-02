package com.asialocalguide.gateway.core.domain.destination;

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
public class DestinationProviderMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    @Setter
    @NotNull
    private Destination destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @Setter
    @NotNull
    private BookingProvider provider;

    @Setter
    @NotNull
    @NotEmpty
    private String providerDestinationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DestinationProviderMapping mapping = (DestinationProviderMapping) o;

        if (id != null && mapping.id != null) {
            return Objects.equals(id, mapping.id);
        }

        return Objects.equals(provider, mapping.provider) &&
                Objects.equals(providerDestinationId, mapping.providerDestinationId);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }

        return Objects.hash(provider, providerDestinationId);
    }
}
