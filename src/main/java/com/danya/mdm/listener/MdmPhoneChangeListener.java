package com.danya.mdm.listener;

import com.danya.mdm.dto.ChangePhoneDto;
import com.danya.mdm.exception.MdmException;
import com.danya.mdm.service.MessageProcessingService;
import com.danya.mdm.service.ValidationService;
import com.danya.mdm.util.JsonUtil;
import lombok.RequiredArgsConstructor;
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

    private final JsonUtil jsonUtil;
    private final MessageProcessingService messageProcessingService;
    private final ValidationService validationService;


    @KafkaListener(topics = "${mdm.kafka.send-mdm-in.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void receiveResponse(ConsumerRecord<String, String> consumerRecord) {
        try {
            log.info("Получено сообщение из топика {}: {}", consumerRecord.topic(), consumerRecord.value());

            ChangePhoneDto dto = jsonUtil.fromJson(consumerRecord.value(), ChangePhoneDto.class);

            validationService.validate(dto);
            messageProcessingService.process(dto);
        } catch (MdmException e) {
            log.warn("Произошла ошибка при обработке сообщения: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Произошла ошибка при обработке сообщения: {}", e.getMessage(), e);
        }
    }
}
