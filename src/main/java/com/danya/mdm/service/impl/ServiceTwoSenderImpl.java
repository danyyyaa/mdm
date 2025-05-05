package com.danya.mdm.service.impl;

import com.danya.mdm.client.service.ServiceTwoClientService;
import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.dto.ServiceTwoUpdatePhoneRequestDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import com.danya.mdm.enums.EventType;
import com.danya.mdm.service.ServiceTwoSender;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceTwoSenderImpl implements ServiceTwoSender {

    private final ServiceTwoClientService serviceTwoClient;

    @Override
    @SneakyThrows
    public ResponseEntity<ServiceUpdatePhoneResponseDto> send(ServiceTwoUpdatePhoneRequestDto dto) {
        return serviceTwoClient.send(dto).get();
    }

    @Override
    public ServiceTwoUpdatePhoneRequestDto createServiceTwoRequest(ChangePhoneDto dto) {
        return ServiceTwoUpdatePhoneRequestDto.builder()
                .id(dto.id())
                .systemId("@danyazaikin")
                .events(List.of(new ServiceTwoUpdatePhoneRequestDto.Event(
                        EventType.CHANGE_PHONE.getValue(),
                        dto.guid(),
                        dto.phone()
                )))
                .build();
    }
}
