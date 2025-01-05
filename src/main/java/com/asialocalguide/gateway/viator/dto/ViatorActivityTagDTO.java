package com.asialocalguide.gateway.viator.dto;

import java.util.List;

public record ViatorActivityTagDTO(
    Long tagId, List<Long> parentTagIds, ViatorActivityTagName allNamesByLocale) {}
