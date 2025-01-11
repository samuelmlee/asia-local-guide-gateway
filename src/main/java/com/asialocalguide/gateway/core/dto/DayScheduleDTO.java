package com.asialocalguide.gateway.core.dto;

import com.asialocalguide.gateway.viator.dto.ViatorActivityDTO;
import java.util.List;

public record DayScheduleDTO(String timeStamp, List<ViatorActivityDTO> activities) {}
