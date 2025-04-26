package com.danya.mdm.dto;

import com.danya.mdm.enums.MdmEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ChangePhoneDto(

        @NotNull(message = "id не может быть null")
        UUID id,
        @NotNull(message = "type не может быть null")
        MdmEventType type,
        @Size(min = 32, max = 32, message = "guid должен содержать ровно 32 символа")
        String guid,
        @Pattern(regexp = "\\+7\\d{10}", message = "Номер телефона должен соответствовать формату +7000000000")
        String phone
) {
}
