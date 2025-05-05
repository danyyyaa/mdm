package com.danya.mdm.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ServiceOneUpdatePhoneRequestDto(
        Meta meta,
        Body body
) {

    @Builder
    public record Meta(
            String systemId,
            String sender
    ) { }

    @Builder
    public record Body(
            UUID id,
            String guid,
            String phone
    ) { }
}
