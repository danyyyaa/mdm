package com.danya.mdm.service;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.dto.ServiceTwoUpdatePhoneRequestDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import org.springframework.http.ResponseEntity;

public interface ServiceTwoSender {

    ResponseEntity<ServiceUpdatePhoneResponseDto> send(ServiceTwoUpdatePhoneRequestDto dto);
    ServiceTwoUpdatePhoneRequestDto createServiceTwoRequest(ChangePhoneDto dto);
}
