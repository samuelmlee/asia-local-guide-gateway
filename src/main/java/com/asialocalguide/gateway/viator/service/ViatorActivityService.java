package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ViatorActivityService {

  private final ViatorClient viatorClient;

  public ViatorActivityService(ViatorClient viatorClient) {
    this.viatorClient = viatorClient;
  }

  public List<ViatorActivityDetailDTO> getActivityDetails(
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

                              if (availabilityOpt.isEmpty()) {
                                throw new IllegalStateException(
                                    "No availability returned for product code: "
                                        + activity.productCode());
                              }

                              return new ViatorActivityDetailDTO(activity, availabilityOpt.get());
                            })
                        .exceptionally(
                            ex -> {
                              log.error(
                                  "Error fetching Viator activity availability for product code: {}",
                                  activity.productCode(),
                                  ex);
                              return null;
                            }))
            .toList();

    CompletableFuture.allOf(futureDetails.toArray(new CompletableFuture[0])).join();

    return futureDetails.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
  }
}
