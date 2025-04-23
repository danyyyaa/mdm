package com.danya.mdm.listener;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mdm.kafka.send-mdm-in", name = "enabled", havingValue = "true")
public class MdmPhoneChangeListener {

    private final ObjectMapper objectMapper;
    private final ValidationService validationService;

    @SneakyThrows
    @KafkaListener(topics = "${mdm.kafka.send-mdm-in.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void receiveResponse(ConsumerRecord<String, String> consumerRecord) {
        log.info("Получено сообщение из топика {}", consumerRecord.topic());

        ChangePhoneDto dto = objectMapper.readValue(consumerRecord.value(), ChangePhoneDto.class);
        validationService.validate(dto);
    }
}
