package com.danya.mdm.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ServiceTwoUpdatePhoneRequestDto(
        UUID id,
        String systemId,
        List<Event> events
) {

    public record Event(
            String eventType,
            String guid,
            String phone
    ) {
    }
}