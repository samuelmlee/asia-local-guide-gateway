package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;

@Service
public class ViatorActivityService {

  private final ViatorClient viatorClient;

  public ViatorActivityService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<ViatorActivityDetailDTO> getActivityDTOs(
      SupportedLocale defaultLocale, ViatorActivitySearchDTO searchDTO) {

    List<ViatorActivityDTO> activities =
        viatorClient.getActivitiesByRequestAndLocale(defaultLocale.getCode(), searchDTO);

    List<CompletableFuture<ViatorActivityDetailDTO>> futureDetails =
        activities.stream()
            .map(
                activity ->
                    CompletableFuture.supplyAsync(
                        () -> {
                          Optional<ViatorActivityAvailabilityDTO> availabilityOpt =
                              viatorClient.getAvailabilityByProductCode(activity.productCode());

                          return new ViatorActivityDetailDTO(
                              activity, availabilityOpt.orElse(null));
                        }))
            .toList();

    CompletableFuture.allOf(futureDetails.toArray(new CompletableFuture[0])).join();

    // 4. Gather results into a List<ViatorActivityDetailDTO>
    return futureDetails.stream().map(CompletableFuture::join).toList();
  }
}
