package com.danya.mdm.service;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.dto.ServiceOneUpdatePhoneRequestDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import org.springframework.http.ResponseEntity;

public interface ServiceOneSender {

    ResponseEntity<ServiceUpdatePhoneResponseDto> send(ServiceOneUpdatePhoneRequestDto dto);
    ServiceOneUpdatePhoneRequestDto createServiceOneRequest(ChangePhoneDto dto);
}
