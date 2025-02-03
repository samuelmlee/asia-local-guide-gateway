package com.asialocalguide.gateway.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayActivityDTO {

  private String productCode;
  private String title;
  private String description;

  private Double combinedAverageRating;
  private Integer reviewCount;

  private Integer durationMinutes;

  private Double fromPrice;

  private List<ImageDTO> images;

  private String providerUrl;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime startTime;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime endTime;
}
