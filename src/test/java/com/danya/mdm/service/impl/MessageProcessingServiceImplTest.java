package com.danya.mdm.service.impl;

import com.danya.mdm.dto.*;
import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.model.MdmMessage;
import com.danya.mdm.model.MdmMessageOutbox;
import com.danya.mdm.repository.MdmMessageOutboxRepository;
import com.danya.mdm.repository.MdmMessageRepository;
import com.danya.mdm.service.ServiceOneSender;
import com.danya.mdm.service.ServiceTwoSender;
import com.danya.mdm.service.TransactionalService;
import com.danya.mdm.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.danya.mdm.enums.MdmDeliveryStatus.DELIVERED;
import static com.danya.mdm.enums.MdmDeliveryStatus.NEW;
import static com.danya.mdm.enums.MdmEventType.USER_PHONE_CHANGE;
import static com.danya.mdm.enums.MdmServiceTarget.USER_DATA_SERVICE_ONE;
import static com.danya.mdm.enums.MdmServiceTarget.USER_DATA_SERVICE_TWO;
import static com.danya.mdm.enums.ResponseStatus.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageProcessingServiceImplTest {

    private static UUID businessId = UUID.randomUUID();
    private static ChangePhoneDto dto = ChangePhoneDto.builder()
            .id(businessId)
            .guid(UUID.randomUUID().toString())
            .type(USER_PHONE_CHANGE)
            .phone("+71234567890")
            .build();

    @Mock
    private MdmMessageRepository messageRepository;
    @Mock
    private MdmMessageOutboxRepository outboxRepository;
    @Mock
    private JsonUtil jsonUtil;
    @Mock
    private TransactionalService transactionalService;
    @Mock
    private ServiceOneSender serviceOneSender;
    @Mock
    private ServiceTwoSender serviceTwoSender;

    @InjectMocks
    private MessageProcessingServiceImpl service;

    @Test
    void process_newMessage_sendsBoth_andMarksDelivered() {
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        })
                .when(transactionalService).runInNewTransaction(any());

        when(messageRepository.findByExternalId(businessId))
                .thenReturn(Optional.empty());
        MdmMessage saved = MdmMessage.builder().externalId(businessId).build();
        when(messageRepository.save(any())).thenReturn(saved);

        MdmMessageOutbox ob1 = MdmMessageOutbox.builder()
                .mdmMessageId(businessId).target(USER_DATA_SERVICE_ONE).status(NEW).build();
        MdmMessageOutbox ob2 = MdmMessageOutbox.builder()
                .mdmMessageId(businessId).target(USER_DATA_SERVICE_TWO).status(NEW).build();

        when(outboxRepository.save(any(MdmMessageOutbox.class)))
                .thenReturn(ob1, ob2);

        ServiceUpdatePhoneResponseDto respDto = ServiceUpdatePhoneResponseDto.builder()
                .body(new ServiceUpdatePhoneResponseDto.Body(businessId, SUCCESS, null))
                .build();
        ResponseEntity<ServiceUpdatePhoneResponseDto> ok = ResponseEntity.ok(respDto);

        when(jsonUtil.toJson(eq(dto))).thenReturn("{dtoJson}");
        when(jsonUtil.toJson(eq(respDto))).thenReturn("{respJson}");

        ServiceOneUpdatePhoneRequestDto req1 = mock(ServiceOneUpdatePhoneRequestDto.class);
        ServiceTwoUpdatePhoneRequestDto req2 = mock(ServiceTwoUpdatePhoneRequestDto.class);
        when(serviceOneSender.createServiceOneRequest(dto)).thenReturn(req1);
        when(serviceOneSender.send(req1)).thenReturn(ok);
        when(serviceTwoSender.createServiceTwoRequest(dto)).thenReturn(req2);
        when(serviceTwoSender.send(req2)).thenReturn(ok);

        service.process(dto);

        verify(transactionalService).runInNewTransaction(any());
        verify(messageRepository).save(any());
        verify(outboxRepository, times(2)).save(any());
        verify(serviceOneSender).send(req1);
        verify(serviceTwoSender).send(req2);
        verify(outboxRepository, times(2))
                .updateDeliveryStatusByMdmMessageId(eq(businessId), eq(DELIVERED), any(ResponseDataDto.class));
    }


    @Test
    void process_existingMessage_onlyRetriesFailed() {
        when(messageRepository.findByExternalId(businessId))
                .thenReturn(Optional.of(mock(MdmMessage.class)));
        MdmMessageOutbox ob = MdmMessageOutbox.builder()
                .mdmMessageId(businessId)
                .target(USER_DATA_SERVICE_ONE)
                .status(MdmDeliveryStatus.ERROR).build();
        when(outboxRepository.findByMdmMessageIdAndStatusIn(eq(businessId), anyList()))
                .thenReturn(List.of(ob));

        ServiceUpdatePhoneResponseDto respDto = ServiceUpdatePhoneResponseDto.builder()
                .body(new ServiceUpdatePhoneResponseDto.Body(businessId, SUCCESS, null))
                .build();
        ResponseEntity<ServiceUpdatePhoneResponseDto> ok = ResponseEntity.ok(respDto);
        when(jsonUtil.toJson(eq(respDto))).thenReturn("{resp}");

        ServiceOneUpdatePhoneRequestDto req1 = mock(ServiceOneUpdatePhoneRequestDto.class);
        when(serviceOneSender.createServiceOneRequest(dto)).thenReturn(req1);
        when(serviceOneSender.send(req1)).thenReturn(ok);

        service.process(dto);

        verify(messageRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());

        verify(serviceOneSender).send(req1);
        verify(outboxRepository)
                .updateDeliveryStatusByMdmMessageId(eq(businessId), eq(DELIVERED), any(ResponseDataDto.class));
    }

    @Test
    void process_senderThrows_marksError() {
        when(messageRepository.findByExternalId(businessId))
                .thenReturn(Optional.of(mock(MdmMessage.class)));
        MdmMessageOutbox ob = MdmMessageOutbox.builder()
                .mdmMessageId(businessId)
                .target(USER_DATA_SERVICE_ONE)
                .status(NEW).build();
        when(outboxRepository.findByMdmMessageIdAndStatusIn(eq(businessId), anyList()))
                .thenReturn(List.of(ob));

        ServiceOneUpdatePhoneRequestDto req1 = mock(ServiceOneUpdatePhoneRequestDto.class);
        when(serviceOneSender.createServiceOneRequest(dto)).thenReturn(req1);
        when(serviceOneSender.send(req1)).thenThrow(new RuntimeException("oops"));

        service.process(dto);

        verify(outboxRepository)
                .updateDeliveryStatusByMdmMessageId(eq(businessId), eq(MdmDeliveryStatus.FATAL_ERROR), any(ResponseDataDto.class));
    }
}
