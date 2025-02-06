package com.asialocalguide.gateway.viator.service;

import com.asialocalguide.gateway.core.config.SupportedLocale;
import com.asialocalguide.gateway.viator.client.ViatorClient;
import com.asialocalguide.gateway.viator.dto.ViatorActivityAvailabilityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivityDetailDTO;
import com.asialocalguide.gateway.viator.dto.ViatorActivitySearchDTO;
import java.util.*;
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
        viatorClient.getActivitiesByRequestAndLocale(defaultLocale.getCode(), searchDTO).stream()
            // No activities with zero duration should be returned
            .filter(dto -> dto.getDurationMinutes() > 0)
            .toList();

    Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> availabilityFutures =
        getIdCompletableFutureMap(activities);

    CompletableFuture.allOf(availabilityFutures.values().toArray(new CompletableFuture[0])).join();

    return activities.stream()
        .map(
            activity -> {
              Optional<ViatorActivityAvailabilityDTO> availabilityOpt =
                  availabilityFutures.get(activity.productCode()).join();

              return availabilityOpt
                  .map(availabilityDTO -> new ViatorActivityDetailDTO(activity, availabilityDTO))
                  .orElse(null);
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>>
      getIdCompletableFutureMap(List<ViatorActivityDTO> activities) {
    Map<String, CompletableFuture<Optional<ViatorActivityAvailabilityDTO>>> availabilityFutures =
        new HashMap<>();
    for (ViatorActivityDTO activity : activities) {
      availabilityFutures.computeIfAbsent(
          activity.productCode(),
          productCode ->
              CompletableFuture.supplyAsync(
                      () -> viatorClient.getAvailabilityByProductCode(productCode))
                  .exceptionally(
                      ex -> {
                        log.error(
                            "Error fetching activity availability for productCode: {}",
                            productCode);
                        return Optional.empty();
                      }));
    }
    return availabilityFutures;
  }
}
