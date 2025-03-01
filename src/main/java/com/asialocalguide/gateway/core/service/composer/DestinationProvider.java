package com.asialocalguide.gateway.core.service.composer;

import com.asialocalguide.gateway.core.domain.BookingProviderName;
import com.asialocalguide.gateway.core.domain.destination.CrossPlatformDestination;
import java.util.List;

public interface DestinationProvider {

  BookingProviderName getProviderName();

  List<CrossPlatformDestination> getDestinations();
}
