package com.danya.mdm.client.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ServiceOneUpdatePhoneRequestDto(
        Meta meta,
        Body body
) {

    public record Meta(
            String systemId,
            String sender
    ) { }

    public record Body(
            UUID id,
            String guid,
            String phone
    ) { }
}
