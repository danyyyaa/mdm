package com.danya.mdm.client;

import com.danya.mdm.dto.ServiceOneUpdatePhoneRequestDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${mdm.integration.service1-host}/user-data-service-one/update-phone", name = "service-one-client")
public interface ServiceOneClient {

    @PostMapping
    ResponseEntity<ServiceUpdatePhoneResponseDto> send(ServiceOneUpdatePhoneRequestDto dto);
}
