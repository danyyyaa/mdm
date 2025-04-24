package com.danya.mdm.dto;

import com.danya.mdm.enums.ResponseStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ServiceUpdatePhoneResponseDto(
        Body body
) {

    public record Body(
            UUID id,
            ResponseStatus status,
            String errorMessage
    ) {
    }
}
