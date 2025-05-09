package com.danya.mdm.listener;

import com.danya.mdm.AbstractTest;
import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.dto.ServiceUpdatePhoneResponseDto;
import com.danya.mdm.enums.MdmDeliveryStatus;
import com.danya.mdm.enums.MdmEventType;
import com.danya.mdm.enums.ResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class MdmPhoneChangeListenerTest extends AbstractTest {

    private static final String SERVICE_ONE_PATH = "/user-data-service-one/update-phone";
    private static final String SERVICE_TWO_PATH = "/user-data-service-two/user/update/phone";

    @Test
    @DisplayName("Успешное обновление телефона")
    void fullFlow_delivered() throws Exception {
        var dto = ChangePhoneDto.builder()
                .id(UUID.randomUUID())
                .guid(UUID.randomUUID().toString().substring(0, 32))
                .type(MdmEventType.USER_PHONE_CHANGE)
                .phone("+71234567890")
                .build();

        var payload = objectMapper.writeValueAsString(dto);
        var responseDto = ServiceUpdatePhoneResponseDto.builder()
                .body(new ServiceUpdatePhoneResponseDto.Body(
                        dto.id(), ResponseStatus.SUCCESS, null))
                .build();
        var responseJson = objectMapper.writeValueAsString(responseDto);

        wireMockServer.stubFor(
                post(urlEqualTo(SERVICE_ONE_PATH))
                        .willReturn(okJson(responseJson))
        );
        wireMockServer.stubFor(
                post(urlEqualTo(SERVICE_TWO_PATH))
                        .willReturn(okJson(responseJson))
        );

        kafkaTemplate.send("phone-change-topic", payload).get();
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(mdmMessageRepository.findByExternalId(dto.id())).isPresent();

            var outboxes = mdmMessageOutboxRepository
                    .findByMdmMessageIdAndStatusIn(dto.id(), List.of(MdmDeliveryStatus.DELIVERED));
            assertThat(outboxes).hasSize(2);

            wireMockServer.verify(1, postRequestedFor(urlEqualTo(SERVICE_ONE_PATH))
                    .withRequestBody(matchingJsonPath("$.body.id",    equalTo(dto.id().toString())))
                    .withRequestBody(matchingJsonPath("$.body.guid",  equalTo(dto.guid())))
                    .withRequestBody(matchingJsonPath("$.body.phone", equalTo(dto.phone())))
            );

            wireMockServer.verify(1, postRequestedFor(urlEqualTo(SERVICE_TWO_PATH))
                    .withRequestBody(matchingJsonPath("$.id",                      equalTo(dto.id().toString())))
                    .withRequestBody(matchingJsonPath("$.events[0].eventType",    equalTo("change_phone")))
                    .withRequestBody(matchingJsonPath("$.events[0].guid",         equalTo(dto.guid())))
                    .withRequestBody(matchingJsonPath("$.events[0].phone",        equalTo(dto.phone())))
            );
        });
    }

    @ParameterizedTest(name = "Ошибка валидации: {1}")
    @MethodSource("invalidDtos")
    void whenInvalidDto_thenValidationErrorsAndNoPersist(ChangePhoneDto invalidDto, String expectedViolationMessage) throws Exception {
        var payload = objectMapper.writeValueAsString(invalidDto);
        kafkaTemplate.send("phone-change-topic", payload);
        kafkaTemplate.flush();

        verify(validationService, timeout(5_000).times(1)).validate(invalidDto);

        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        assertThat(mdmMessageRepository.findByExternalId(invalidDto.id())).isEmpty()
                );
    }

    private static Stream<Arguments> invalidDtos() {
        String guid = "0123456789ABCDEF0123456789ABCDEF";
        UUID id = UUID.randomUUID();

        return Stream.of(
                Arguments.of(ChangePhoneDto.builder().id(null).type(MdmEventType.USER_PHONE_CHANGE).guid(guid).phone("+71234567890").build(),
                        "id не может быть null"),
                Arguments.of(ChangePhoneDto.builder().id(id).type(null).guid(guid).phone("+71234567890").build(),
                        "type не может быть null"),
                Arguments.of(ChangePhoneDto.builder().id(id).type(MdmEventType.USER_PHONE_CHANGE).guid("SHORT_GUID").phone("+71234567890").build(),
                        "guid должен содержать ровно 32 символа"),
                Arguments.of(ChangePhoneDto.builder().id(id).type(MdmEventType.USER_PHONE_CHANGE).guid(guid).phone("12345").build(),
                        "Номер телефона должен соответствовать формату +7000000000")
        );
    }

}
