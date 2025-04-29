package com.danya.mdm.service.impl;

import com.danya.mdm.client.service.ServiceOneClientService;
import com.danya.mdm.client.service.ServiceTwoClientService;
import com.danya.mdm.dto.*;
import com.danya.mdm.enums.EventType;
import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.enums.MdmServiceTarget;
import com.danya.mdm.enums.ResponseStatus;
import com.danya.mdm.model.MdmMessage;
import com.danya.mdm.model.MdmMessageOutbox;
import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.repository.MdmMessageRepository;
import com.danya.mdm.service.MessageProcessingService;
import com.danya.mdm.service.TransactionalService;
import com.danya.mdm.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingServiceImpl implements MessageProcessingService {

    private final MdmMessageRepository messageRepository;
    private final MdmMessageOutboxRepository outboxRepository;
    private final ServiceOneClientService serviceOneClient;
    private final ServiceTwoClientService serviceTwoClient;
    private final JsonUtil jsonUtil;
    private final TransactionalService transactionalService;
    private final ObjectMapper objectMapper;

    @Override
    public void process(ChangePhoneDto dto) {
        transactionalService.runInNewTransaction(() -> {
            MdmMessage saved = saveMessage(dto);
            saveOutbox(saved.getExternalId(), MdmServiceTarget.USER_DATA_SERVICE_ONE);
            saveOutbox(saved.getExternalId(), MdmServiceTarget.USER_DATA_SERVICE_TWO);
        });

        sendToServiceOne(dto);
        sendToServiceTwo(dto);
    }

    @Override
    public void process(List<MdmMessageOutbox> batch) {
        for (MdmMessageOutbox outbox : batch) {
            try {
                UUID businessId = outbox.getMdmMessageId();
                MdmMessage msg = messageRepository.findByExternalId(businessId)
                        .orElseThrow(() -> new IllegalStateException("Сообщения не найдено для externalId: " + businessId));

                ChangePhoneDto dto = jsonUtil.fromJson(msg.getPayload().toString(), ChangePhoneDto.class);
                if (outbox.getTarget() == MdmServiceTarget.USER_DATA_SERVICE_ONE) {
                    sendToServiceOne(dto);
                } else {
                    sendToServiceTwo(dto);
                }
            } catch (Exception e) {
                log.warn("Ошибка при обработке сообщения с id {}: {}", outbox.getMdmMessageId(), e.getMessage(), e);
            }
        }
    }

    private MdmMessage saveMessage(ChangePhoneDto dto) {
        JsonNode payload = objectMapper.valueToTree(dto);
        MdmMessage message = MdmMessage.builder()
                .externalId(dto.id())
                .guid(dto.guid())
                .type(dto.type())
                .payload(payload)
                .build();
        return messageRepository.save(message);
    }

    private void saveOutbox(UUID externalId, MdmServiceTarget target) {
        MdmMessageOutbox outbox = MdmMessageOutbox.builder()
                .mdmMessageId(externalId)
                .status(MdmDeliveryStatus.NEW)
                .target(target)
                .responseData(null)
                .build();
        outboxRepository.save(outbox);
    }

    private void sendToServiceOne(ChangePhoneDto dto) {
        var request = createServiceOneRequest(dto);
        try {
            ResponseEntity<ServiceUpdatePhoneResponseDto> resp = serviceOneClient.send(request).get();
            handleResponse(resp, dto.id());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Отправка в Сервис 1 прервана, id={}", dto.id(), ie);
            handleError(dto.id(), ie.getMessage(), null, "Сервис 1");
        } catch (Exception e) {
            log.error("Ошибка при отправке в Сервис 1, id={}: {}", dto.id(), e.getMessage(), e);
            handleError(dto.id(), e.getMessage(), null, "Сервис 1");
        }
    }

    private void sendToServiceTwo(ChangePhoneDto dto) {
        var request = createServiceTwoRequest(dto);
        try {
            ResponseEntity<ServiceUpdatePhoneResponseDto> resp = serviceTwoClient.send(request).get();
            handleResponse(resp, dto.id());
        } catch (FeignException fe) {
            handleError(dto.id(), fe.contentUTF8(), fe.status(), "Сервис 2");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Отправка в Сервис 2 прервана, id={}", dto.id(), ie);
            handleError(dto.id(), ie.getMessage(), null, "Сервис 2");
        } catch (Exception e) {
            log.error("Ошибка при отправке в Сервис 2, id={}: {}", dto.id(), e.getMessage(), e);
            handleError(dto.id(), e.getMessage(), null, "Сервис 2");
        }
    }

    private void handleResponse(ResponseEntity<ServiceUpdatePhoneResponseDto> response,
                                UUID businessId) {
        ServiceUpdatePhoneResponseDto body = response.getBody();
        String raw = jsonUtil.toJson(body);

        MdmDeliveryStatus status = determineStatus(response, body, businessId);
        List<String> errors = status == MdmDeliveryStatus.ERROR
                ? extractErrorMessages(body)
                : Collections.emptyList();

        ResponseDataDto data = ResponseDataDto.builder()
                .deserializedResponse(raw)
                .errorMessages(errors)
                .build();

        outboxRepository.updateDeliveryStatusById(businessId, status, data);
    }

    private void handleError(UUID businessId,
                             String errorMessage,
                             Integer httpStatus,
                             String serviceName) {
        ResponseDataDto data = ResponseDataDto.builder()
                .deserializedResponse(errorMessage)
                .errorMessages(List.of(errorMessage))
                .build();

        outboxRepository.updateDeliveryStatusById(businessId, MdmDeliveryStatus.ERROR, data);

        if (httpStatus != null) {
            log.warn("{} вернул {} для сообщения {}: {}", serviceName, httpStatus, businessId, errorMessage);
        } else {
            log.error("Неожиданная ошибка при отправке в {} для сообщения {}: {}", serviceName, businessId, errorMessage);
        }
    }

    private ServiceOneUpdatePhoneRequestDto createServiceOneRequest(ChangePhoneDto dto) {
        return ServiceOneUpdatePhoneRequestDto.builder()
                .meta(ServiceOneUpdatePhoneRequestDto.Meta.builder()
                        .systemId("@danyazaikin")
                        .sender("@danyazaikin")
                        .build())
                .body(ServiceOneUpdatePhoneRequestDto.Body.builder()
                        .id(dto.id())
                        .guid(dto.guid())
                        .phone(dto.phone())
                        .build())
                .build();
    }

    private ServiceTwoUpdatePhoneRequestDto createServiceTwoRequest(ChangePhoneDto dto) {
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

    private MdmDeliveryStatus determineStatus(ResponseEntity<ServiceUpdatePhoneResponseDto> response,
                                              ServiceUpdatePhoneResponseDto dto,
                                              UUID businessId) {
        if (!response.getStatusCode().is2xxSuccessful() ||
                Optional.ofNullable(dto)
                        .map(ServiceUpdatePhoneResponseDto::body)
                        .map(ServiceUpdatePhoneResponseDto.Body::status)
                        .orElse(ResponseStatus.FAILED) != ResponseStatus.SUCCESS) {
            log.warn("Ошибка доставки для {}: HTTP {}, response.status={}",
                    businessId,
                    response.getStatusCode().value(),
                    Optional.ofNullable(dto)
                            .map(ServiceUpdatePhoneResponseDto::body)
                            .map(ServiceUpdatePhoneResponseDto.Body::status)
                            .orElse(ResponseStatus.FAILED));
            return MdmDeliveryStatus.ERROR;
        }
        log.info("Сообщение {} успешно доставлено", businessId);
        return MdmDeliveryStatus.DELIVERED;
    }

    private List<String> extractErrorMessages(ServiceUpdatePhoneResponseDto dto) {
        return Optional.ofNullable(dto)
                .map(ServiceUpdatePhoneResponseDto::body)
                .map(ServiceUpdatePhoneResponseDto.Body::errorMessage)
                .map(List::of)
                .orElse(Collections.emptyList());
    }
}
