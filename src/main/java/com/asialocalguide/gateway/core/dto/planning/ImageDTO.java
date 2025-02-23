package com.asialocalguide.gateway.core.dto.planning;

import java.util.List;

public record ImageDTO(
    String imageSource, String caption, boolean isCover, List<VariantDTO> variants) {
  public record VariantDTO(Integer height, Integer width, String url) {}
}
