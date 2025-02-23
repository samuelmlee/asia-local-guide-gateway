package com.asialocalguide.gateway.core.domain.destination;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.dto.destination.RawDestinationDTO;
import java.util.List;

public record DestinationIngestionInput(BookingProviderName providerName, List<RawDestinationDTO> rawDestinations) {}
