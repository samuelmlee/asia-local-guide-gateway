package com.asialocalguide.gateway.viator.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

public record ViatorDestinationDTO(Long destinationId, String name, String type) {}
