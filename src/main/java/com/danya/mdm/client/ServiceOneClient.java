package com.danya.mdm.client;

import com.danya.mdm.client.dto.ServiceOneUpdatePhoneRequestDto;
import com.danya.mdm.client.dto.ServiceUpdatePhoneResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(url = "${app.integration.service1-host}/user-data-service-one/update-phone", name = "service-one-client")
public interface ServiceOneClient {

    @PostMapping
    ServiceUpdatePhoneResponseDto send(ServiceOneUpdatePhoneRequestDto dto);
}
