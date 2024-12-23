package com.asialocalguide.gateway.viator.dto;

public record ViatorDestinationDTO(
    Long destinationId, Long parentDestinationId, String name, String type) {}
