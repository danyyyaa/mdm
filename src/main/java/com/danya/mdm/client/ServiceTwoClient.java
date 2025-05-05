package com.danya.mdm.client;

import com.danya.mdm.dto.ServiceTwoUpdatePhoneRequestDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${mdm.integration.service2-host}/user-data-service-two/user/update/phone", name = "service-two-client")
public interface ServiceTwoClient {

    @PostMapping
    ResponseEntity<ServiceUpdatePhoneResponseDto> send(ServiceTwoUpdatePhoneRequestDto dto);
}
