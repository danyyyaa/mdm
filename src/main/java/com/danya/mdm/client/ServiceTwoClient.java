package com.danya.mdm.client;

import com.danya.mdm.client.dto.ServiceTwoUpdatePhoneRequestDto;
import com.danya.mdm.client.dto.ServiceUpdatePhoneResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${app.integration.service2-host}/user-data-service-two/user/update/phone", name = "service-two-client")
public interface ServiceTwoClient {

    @PostMapping
    ServiceUpdatePhoneResponseDto send(ServiceTwoUpdatePhoneRequestDto dto);
}
