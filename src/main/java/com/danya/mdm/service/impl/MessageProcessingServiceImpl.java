package com.danya.mdm.service.impl;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.dto.ResponseDataDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.enums.MdmServiceTarget;
import com.danya.mdm.enums.ResponseStatus;
import com.danya.mdm.model.MdmMessage;
import com.danya.mdm.model.MdmMessageOutbox;
import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.repository.MdmMessageRepository;
import com.danya.mdm.service.MessageProcessingService;
import com.danya.mdm.service.ServiceOneSender;
import com.danya.mdm.service.ServiceTwoSender;
import com.danya.mdm.service.TransactionalService;
import com.danya.mdm.util.JsonUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessingServiceImpl implements MessageProcessingService {

    private final MdmMessageRepository messageRepository;
    private final MdmMessageOutboxRepository outboxRepository;
    private final JsonUtil jsonUtil;
    private final TransactionalService transactionalService;
    private final ServiceOneSender serviceOneSender;
    private final ServiceTwoSender serviceTwoSender;

    @Override
    public void process(ChangePhoneDto dto) {
        boolean isNew = messageRepository.findByExternalId(dto.id()).isEmpty();
        List<MdmMessageOutbox> toSend = new ArrayList<>();

        if (isNew) {
            transactionalService.runInNewTransaction(() -> {
                MdmMessage saved = saveMessage(dto);
                toSend.add(saveOutbox(saved.getExternalId(), MdmServiceTarget.USER_DATA_SERVICE_ONE));
                toSend.add(saveOutbox(saved.getExternalId(), MdmServiceTarget.USER_DATA_SERVICE_TWO));
            });
        } else {
            toSend.addAll(outboxRepository.findByMdmMessageIdAndStatusIn(
                    dto.id(),
                    List.of(MdmDeliveryStatus.NEW, MdmDeliveryStatus.ERROR)
            ));
        }

        toSend.forEach(outbox -> sendToService(dto, outbox.getTarget()));
    }

    private void sendToService(ChangePhoneDto dto, MdmServiceTarget target) {
        try {
            ResponseEntity<ServiceUpdatePhoneResponseDto> resp;
            if (target == MdmServiceTarget.USER_DATA_SERVICE_ONE) {
                var request = serviceOneSender.createServiceOneRequest(dto);
                resp = serviceOneSender.send(request);
            } else {
                var request = serviceTwoSender.createServiceTwoRequest(dto);
                resp = serviceTwoSender.send(request);
            }
            handleResponse(resp, dto.id());
        } catch (FeignException fe) {
            handleError(dto.id(), fe.contentUTF8(), fe.status(), target.name());
        } catch (Exception e) {
            log.error("Ошибка при отправке в {}: {}", target, e.getMessage(), e);
            handleError(dto.id(), e.getMessage(), null, target.name());
        }
    }

    private MdmMessage saveMessage(ChangePhoneDto dto) {
        String payload = jsonUtil.toJson(dto);
        MdmMessage message = MdmMessage.builder()
                .externalId(dto.id())
                .guid(dto.guid())
                .type(dto.type())
                .payload(payload)
                .build();
        return messageRepository.save(message);
    }

    private MdmMessageOutbox saveOutbox(UUID externalId, MdmServiceTarget target) {
        MdmMessageOutbox outbox = MdmMessageOutbox.builder()
                .mdmMessageId(externalId)
                .status(MdmDeliveryStatus.NEW)
                .target(target)
                .responseData(null)
                .build();
        return outboxRepository.save(outbox);
    }

    private void handleResponse(ResponseEntity<ServiceUpdatePhoneResponseDto> response, UUID externalId) {
        ServiceUpdatePhoneResponseDto body = response.getBody();
        String raw = jsonUtil.toJson(body);

        MdmDeliveryStatus status = determineStatus(response, body, externalId);
        List<String> errors = status == MdmDeliveryStatus.ERROR
                ? extractErrorMessages(body)
                : Collections.emptyList();

        ResponseDataDto data = ResponseDataDto.builder()
                .response(raw)
                .errors(errors)
                .build();

        outboxRepository.updateDeliveryStatusByMdmMessageId(externalId, status, data);
    }

    private void handleError(UUID externalId,
                             String errorMessage,
                             Integer httpStatus,
                             String serviceName) {
        ResponseDataDto data = ResponseDataDto.builder()
                .response(errorMessage)
                .errors(List.of(errorMessage))
                .build();

        outboxRepository.updateDeliveryStatusByMdmMessageId(externalId, MdmDeliveryStatus.FATAL_ERROR, data);

        if (httpStatus != null) {
            log.warn("{} вернул {} для сообщения {}: {}", serviceName, httpStatus, externalId, errorMessage);
        } else {
            log.error("Неожиданная ошибка при отправке в {} для сообщения {}: {}", serviceName, externalId, errorMessage);
        }
    }

    private MdmDeliveryStatus determineStatus(ResponseEntity<ServiceUpdatePhoneResponseDto> response,
                                              ServiceUpdatePhoneResponseDto dto,
                                              UUID externalId) {
        if (!response.getStatusCode().is2xxSuccessful() ||
                Optional.ofNullable(dto)
                        .map(ServiceUpdatePhoneResponseDto::body)
                        .map(ServiceUpdatePhoneResponseDto.Body::status)
                        .orElse(ResponseStatus.FAILED) != ResponseStatus.SUCCESS) {
            log.warn("Ошибка доставки для {}: HTTP {}, response.status={}",
                    externalId,
                    response.getStatusCode().value(),
                    Optional.ofNullable(dto)
                            .map(ServiceUpdatePhoneResponseDto::body)
                            .map(ServiceUpdatePhoneResponseDto.Body::status)
                            .orElse(ResponseStatus.FAILED));
            return MdmDeliveryStatus.ERROR;
        }
        log.info("Сообщение {} успешно доставлено", externalId);
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
